# Deploy do BuscaRM — runbook

Passo a passo para colocar o BuscaRM no ar: **Neon** (Postgres) → **Render** (backend) →
**Vercel** (frontend), com **deploy automático a partir do Git** (ADR-0005). Para o mapa de
_quais_ segredos existem e _onde_ moram, ver [`CONFIGURACAO_NUVEM.md`](./CONFIGURACAO_NUVEM.md).

> **Regra de ouro:** segredo nunca vai para o repositório. Vai para o painel do Render/Vercel
> (variáveis de runtime do serviço).

## Visão geral do fluxo

```
push na main ─► CI verde ─► Render builda backend/Dockerfile e publica  (auto-deploy)
                          └► Vercel rebuilda o frontend e publica       (auto-deploy)

tag vX.Y.Z ──► release.yml ──► imagem GHCR + build do front (artefatos versionados p/ rastreabilidade)
```

- **Deploy é automático:** Render e Vercel estão ligados ao repo e publicam a cada push na `main`.
  Não há deploy hooks nem passo manual — o gatilho é o próprio merge na `main`.
- O Render **builda o `backend/Dockerfile`** direto do repo (não puxa a imagem do GHCR).
- A tag (`release.yml`) só gera **artefatos versionados** (imagem GHCR + build do front) para
  rastreabilidade/rollback; não é a fonte do deploy.
- Depois de publicar, rode o **smoke test** (§6) para confirmar que a produção está de pé.

---

## 0. Pré-requisitos

- Contas gratuitas em **Neon**, **Render** e **Vercel** (login com o GitHub facilita).
- Repositório `joaovneres/buscarm` no GitHub (já existe) com CI verde na `main`.
- `gh` autenticado localmente (já está) para definir os _secrets_.

---

## 1. Banco de dados — Neon (Postgres)

1. Crie um projeto no Neon (região próxima, ex.: AWS `us-east`). Nome do banco: `buscarm`.
2. Copie a **connection string**. Ela vem no formato:
   ```
   postgresql://USUARIO:SENHA@ep-xxxx.us-east-2.aws.neon.tech/buscarm?sslmode=require
   ```
3. **Prefira a conexão direta** (sem `-pooler` no host) para que o Flyway rode as migrations (DDL).
4. Separe em três variáveis para o Render (o backend usa URL e credenciais separadas):

   | Variável | Valor a partir da string do Neon |
   | --- | --- |
   | `DATABASE_URL` | `jdbc:postgresql://ep-xxxx.us-east-2.aws.neon.tech/buscarm?sslmode=require` (host + db, **sem** usuário/senha, mantendo `sslmode=require`) |
   | `DB_USER` | a parte `USUARIO` |
   | `DB_PASSWORD` | a parte `SENHA` |

> As migrations Flyway (`V1`..`V5`) rodam **sozinhas** na primeira subida do backend. Já foram
> **validadas contra Postgres 16** (inclusive o trigger de auditoria do ADR-0006). Para revalidar
> localmente, ver a seção [Validar migrations](#validar-migrations-localmente-opcional).

---

## 2. Backend — Render (Docker buildado do repo)

### 2.1 Criar o serviço

O serviço em produção é um **Web Service Docker ligado ao repositório**, que **builda o
`backend/Dockerfile`** a cada push — não puxa imagem do GHCR. Como está configurado hoje:

| Campo | Valor |
| --- | --- |
| Nome | `buscarm` (URL `https://buscarm.onrender.com`) |
| Runtime | Docker (`dockerfilePath: ./Dockerfile`, `dockerContext: .`) |
| Root Directory | `backend` |
| Branch | `main` |
| Auto-Deploy | **On** (publica a cada push na `main`) |
| Health Check Path | `/actuator/health` |

Para recriar: Render → **New → Web Service** → conecte `joaovneres/buscarm` → Root Directory
`backend` → Runtime **Docker** → Branch `main` → Auto-Deploy **On**. (O [`render.yaml`](../render.yaml)
do repo serve de referência de blueprint.)

### 2.3 Variáveis de ambiente (Environment)

| Variável | Valor |
| --- | --- |
| `DATABASE_URL` | JDBC do Neon (passo 1) |
| `DB_USER` / `DB_PASSWORD` | credenciais do Neon |
| `JWT_SECRET` | segredo forte (≥ 32 bytes) — o Blueprint gera; ou `openssl rand -base64 48` |
| `CORS_ALLOWED_ORIGINS` | **preencher depois** com a URL do Vercel (passo 3) |
| `ADMIN_USERNAME` | usuário do admin inicial (login é por username — ver V7; ex.: `secretaria`) |
| `ADMIN_PASSWORD` | senha forte do admin inicial (**não** use o default) |

Suba o serviço. A URL pública é `https://buscarm.onrender.com`.

### 2.4 Publicação (automática)

Com **Auto-Deploy On**, cada push na `main` dispara build + publish no Render — não há deploy
hook nem passo manual. Para republicar sem novo commit (ex.: após mudar uma env var), use
**Manual Deploy → Deploy latest commit** no painel do Render.

---

## 3. Frontend — Vercel (Next.js)

1. Vercel → **Add New → Project** → importe `joaovneres/buscarm`.
2. **Root Directory = `frontend`** (importante — o Next fica em subpasta). O framework é fixado
   como Next.js pelo [`frontend/vercel.json`](../frontend/vercel.json) (sem ele, o Vercel pode não
   autodetectar e falhar com _"No Output Directory named 'public'"_).
3. Variável de ambiente `NEXT_PUBLIC_API_URL` (URL do backend no Render). **Já vem fixada** no
   [`frontend/vercel.json`](../frontend/vercel.json) (`build.env`) como `https://buscarm.onrender.com`,
   então o build do Vercel já aponta para o backend sem precisar mexer no painel. Se a URL do
   backend mudar, atualize esse arquivo (a variável é inlinada no build; exige novo deploy).
   Para sobrescrever pontualmente, defina `NEXT_PUBLIC_API_URL` nas Environment Variables do projeto.
4. Deploy. Anote a URL, ex.: `https://buscarm.vercel.app`.
5. **Publicação (automática):** com o projeto ligado ao Git, a Vercel publica **produção** a cada
   push na `main` e gera **preview** por PR. Não há deploy hook nem passo manual.

---

## 4. Fechar o laço do CORS

Volte ao **Render** e defina `CORS_ALLOWED_ORIGINS` = URL do Vercel (ex.: `https://buscarm.vercel.app`),
depois **redeploy** o backend (Manual Deploy → Deploy latest commit). Sem isso o navegador bloqueia
as chamadas.

> Ordem resumida: **backend primeiro** (para ter a URL) → frontend com essa URL → volta e ajusta o
> CORS do backend → redeploy do backend.

---

## 5. Publicar uma nova versão (uso recorrente)

O deploy é **automático**: basta o merge na `main` (Render e Vercel publicam sozinhos). Fluxo:

```bash
# abra um PR, deixe o CI verde (backend/frontend/e2e), faça o merge na main.
# → Render builda backend/Dockerfile e publica; Vercel rebuilda o frontend.
```

Opcionalmente, marque a versão para rastreabilidade (gera imagem GHCR + build do front como
artefatos; **não** afeta o deploy):

```bash
git tag v0.2.0 && git push origin v0.2.0     # dispara release.yml (artefatos versionados)
```

Depois de publicar, rode o **smoke test** (§6).

---

## 6. Smoke test em produção (T11.7)

Automatizado em **Actions → Smoke (produção) → Run workflow** (`.github/workflows/smoke.yml`):
informe `backend_url` (default `https://buscarm.onrender.com`) e, opcionalmente, `frontend_url`.
Ele já lida com o cold start do free tier (repete até o serviço acordar).

Manualmente, se preferir:

```bash
# 1) Saúde do backend
curl -s https://buscarm.onrender.com/actuator/health      # {"status":"UP"}

# 2) Login (admin inicial) — o campo é "username" (não "email"; ver migration V7)
curl -s -X POST https://buscarm.onrender.com/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"SEU_ADMIN_USERNAME","senha":"SUA_ADMIN_SENHA"}'   # retorna { token, ... }

# 3) Busca autenticada
curl -s https://buscarm.onrender.com/api/registros \
  -H "Authorization: Bearer <TOKEN>"
```

No navegador: abra a URL do Vercel, faça login, cadastre um registro, busque, exporte. Pronto.

> **Primeira chamada lenta?** No plano free o Render hiberna após inatividade; a primeira requisição
> "acorda" o serviço (~30 s). Normal.

---

## Manter o backend acordado (keep-alive)

O Render free hiberna após ~15 min sem tráfego. O keep-alive precisa, portanto, pingar em
intervalos **menores que 15 min** — e essa é a parte que o GitHub Actions não entrega.

### Não use `schedule` do GitHub Actions para isto

O `schedule` é **best-effort**: o GitHub descarta e atrasa crons de alta frequência. Medição feita
neste repositório em out/2026, com o cron `*/10 10-21 * * 1-5` (72 execuções/dia esperadas):

| Métrica | Esperado | Medido |
| --- | --- | --- |
| Execuções por dia útil | 72 | **1 a 4** |
| Intervalo entre pings | 10 min | mediana **4h06m**, mínimo 2h01m, máximo 68h |
| Intervalos acima de 15 min | 0 | **99 de 99 (100%)** |
| Execuções desde 10/ago | ~3.600 | 347 (~10%) |
| Fora da janela `10-21 UTC` | 0 | 29 de 100 |

Como **todos** os intervalos passaram de 15 min, o workflow agendado nunca impediu uma
hibernação: ele apenas acordava o serviço ~3x/dia. As execuções fora da janela (22:12, 23:39,
00:24 UTC) são runs enfileirados com horas de atraso — a confirmação de que o `*/10` não é honrado.

Um agravante: `schedule` é **desativado automaticamente após 60 dias sem atividade** no
repositório, sem aviso no workflow.

### Configuração correta: monitor HTTP externo

Use o [cron-job.org](https://cron-job.org) (grátis, resolução de 1 min e com janela de horário):

| Campo | Valor |
| --- | --- |
| URL | `https://buscarm.onrender.com/actuator/health` |
| Método | `GET` |
| Intervalo | **5 min** |
| Dias | Seg–Sex |
| Horas | `10:00–21:59 UTC` (= 07:00–18:59 BRT) |
| Sucesso esperado | HTTP `200`, corpo com `"status":"UP"` |

O intervalo de 5 min dá folga de 3x sobre os 15 min do Render. A janela Seg–Sex preserva a
intenção original e segura o consumo de horas: 12 h × ~22 dias úteis ≈ **264 h/mês**, bem dentro
das 750 h/mês grátis do Render. Pingar 24/7 também caberia (~730 h), mas sem folga alguma.

O **UptimeRobot** serve igualmente bem, com a ressalva de que o plano grátis monitora 24/7 sem
janela de horário — o que gasta quase toda a cota de horas do Render.

Não aponte o monitor para `/actuator/info`: retorna 401.

### Acordar à mão

O workflow [`keepalive.yml`](../.github/workflows/keepalive.yml) virou **warm-up manual**
(`workflow_dispatch`), útil antes de uma demonstração: **Actions → Warm-up (Render) — manual**.
Ele repete o ping até 5 vezes para tolerar o cold start.

---

## Validar migrations localmente (opcional)

Reproduz o que já foi validado (Postgres 16 + trigger de auditoria):

```bash
docker run -d --name pgtest -e POSTGRES_PASSWORD=buscarm -e POSTGRES_USER=buscarm \
  -e POSTGRES_DB=buscarm postgres:16-alpine
# aplica V1..V5 na ordem
for f in backend/src/main/resources/db/migration/V*.sql; do
  docker exec -i pgtest psql -U buscarm -d buscarm -v ON_ERROR_STOP=1 -q < "$f"; done
docker rm -f pgtest
```

---

## Checklist de produção

- [ ] `JWT_SECRET` forte e único (não o default de dev)
- [ ] `ADMIN_PASSWORD` alterado (não `admin12345`); considere trocar a senha após o 1º login
- [ ] `CORS_ALLOWED_ORIGINS` = exatamente a URL do Vercel (com `https://`, sem barra final)
- [ ] `DATABASE_URL` com `sslmode=require` (Neon exige TLS)
- [ ] Render com Auto-Deploy **On** na `main` e Root Directory `backend`
- [ ] `/actuator/health` responde `UP` e o login funciona (rode o workflow **Smoke**)
