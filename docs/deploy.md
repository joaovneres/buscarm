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

O Render free hiberna após ~15 min sem tráfego. Para evitar o cold start na janela de uso:

- **Janela de horário (recomendado):** o workflow [`keepalive.yml`](../.github/workflows/keepalive.yml)
  pinga `/actuator/health` a cada 10 min **Seg–Sex, ~07:00–19:00 BRT** (cron em UTC: `*/10 10-21 * * 1-5`).
  Para mudar a janela, ajuste o `cron` (lembre: UTC = BRT + 3h). Fora da janela o serviço dorme.
- **24/7:** use o **UptimeRobot** (monitor HTTP a cada 5 min apontando para
  `https://buscarm.onrender.com/actuator/health`). Não use `/actuator/info` (retorna 401).
  Atenção: 24/7 consome quase todas as 750 h/mês grátis do Render.

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
