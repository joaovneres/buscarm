# Deploy do BuscaRM — runbook

Passo a passo para colocar o BuscaRM no ar: **Neon** (Postgres) → **Render** (backend) →
**Vercel** (frontend), com deploy **manual versionado** (ADR-0005). Para o mapa de _quais_
segredos existem e _onde_ moram, ver [`CONFIGURACAO_NUVEM.md`](./CONFIGURACAO_NUVEM.md).

> **Regra de ouro:** segredo nunca vai para o repositório. Vai para o painel do Render/Vercel
> (variáveis de runtime) ou para _secrets_ do GitHub Actions (deploy hooks).

## Visão geral do fluxo

```
tag vX.Y.Z ──► release.yml ──► imagem GHCR  ──► (deploy manual) ──► Render (backend)
                              ghcr.io/joaovneres/buscarm-backend:vX.Y.Z
push na main ─► CI verde ─────► deploy.yml (workflow_dispatch) ──► Vercel (frontend)
```

- **Build** de versão é automático ao criar a tag; **deploy** é manual (você escolhe quando).
- O backend roda a **imagem já construída** no GHCR; o Render só a publica.

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

## 2. Backend — Render (Docker, imagem do GHCR)

### 2.1 Publicar a primeira imagem versionada

```bash
git checkout main && git pull
git tag v0.1.0
git push origin v0.1.0        # dispara release.yml → publica ghcr.io/joaovneres/buscarm-backend:v0.1.0
```

Acompanhe em **Actions → Release**. Ao final existe a imagem no GHCR.

> **Visibilidade do GHCR:** por padrão o pacote é **privado**. Para o Render puxar sem credencial,
> torne-o **público** (GitHub → seu perfil → Packages → `buscarm-backend` → Package settings →
> Change visibility → Public). Alternativa: adicionar uma _registry credential_ no Render.

### 2.2 Criar o serviço

Opção A — **Blueprint** (usa o [`render.yaml`](../render.yaml) do repo): Render → **New → Blueprint**
→ selecione o repositório. Ele cria o serviço `buscarm-backend` já com as variáveis pedindo valor.

Opção B — **manual**: New → Web Service → _Deploy an existing image_ →
`ghcr.io/joaovneres/buscarm-backend:v0.1.0`. Health check path: `/actuator/health`. `autoDeploy` off.

### 2.3 Variáveis de ambiente (Environment)

| Variável | Valor |
| --- | --- |
| `DATABASE_URL` | JDBC do Neon (passo 1) |
| `DB_USER` / `DB_PASSWORD` | credenciais do Neon |
| `JWT_SECRET` | segredo forte (≥ 32 bytes) — o Blueprint gera; ou `openssl rand -base64 48` |
| `CORS_ALLOWED_ORIGINS` | **preencher depois** com a URL do Vercel (passo 3) |
| `ADMIN_EMAIL` | e-mail do admin inicial (ex.: `secretaria@escola.br`) |
| `ADMIN_PASSWORD` | senha forte do admin inicial (**não** use o default) |

Suba o serviço. Anote a URL pública, ex.: `https://buscarm-backend.onrender.com`.

### 2.4 Deploy Hook do backend

Render → serviço → **Settings → Deploy Hook** → copie a URL. Guarde como _secret_ do GitHub:

```bash
gh secret set RENDER_DEPLOY_HOOK_URL --repo joaovneres/buscarm
```

---

## 3. Frontend — Vercel (Next.js)

1. Vercel → **Add New → Project** → importe `joaovneres/buscarm`.
2. **Root Directory = `frontend`** (importante — o Next fica em subpasta). O framework é fixado
   como Next.js pelo [`frontend/vercel.json`](../frontend/vercel.json) (sem ele, o Vercel pode não
   autodetectar e falhar com _"No Output Directory named 'public'"_).
3. Variável de ambiente:
   | Variável | Valor |
   | --- | --- |
   | `NEXT_PUBLIC_API_URL` | URL do backend no Render (passo 2.3), ex.: `https://buscarm-backend.onrender.com` |
4. Deploy. Anote a URL, ex.: `https://buscarm.vercel.app`.
5. **Deploy Hook do frontend:** Settings → Git → **Deploy Hooks** → crie um (branch `main`) → copie:
   ```bash
   gh secret set VERCEL_DEPLOY_HOOK_URL --repo joaovneres/buscarm
   ```

---

## 4. Fechar o laço do CORS

Volte ao **Render** e defina `CORS_ALLOWED_ORIGINS` = URL do Vercel (ex.: `https://buscarm.vercel.app`),
depois **redeploy** o backend (Manual Deploy, ou o passo 5). Sem isso o navegador bloqueia as chamadas.

> Ordem resumida: **backend primeiro** (para ter a URL) → frontend com essa URL → volta e ajusta o
> CORS do backend → redeploy do backend.

---

## 5. Deploy manual versionado (uso recorrente)

Depois que os serviços existem, cada nova versão é: **tag → build automático → deploy manual**.

```bash
git tag v0.2.0 && git push origin v0.2.0     # release.yml publica a imagem :v0.2.0
```

GitHub → **Actions → Deploy → Run workflow**:
- **version:** `v0.2.0`
- **target:** `ambos` (ou só `backend`/`frontend`)

O job usa o _environment_ `producao` (dá para exigir aprovação em Settings → Environments) e dispara
os deploy hooks — Render publica a imagem `:v0.2.0`, Vercel rebuilda o frontend.

---

## 6. Smoke test em produção (T11.7)

```bash
# 1) Saúde do backend
curl -s https://buscarm-backend.onrender.com/actuator/health      # {"status":"UP"}

# 2) Login (admin inicial)
curl -s -X POST https://buscarm-backend.onrender.com/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"SEU_ADMIN_EMAIL","senha":"SUA_ADMIN_SENHA"}'       # retorna { token, ... }

# 3) Busca autenticada
curl -s https://buscarm-backend.onrender.com/api/registros \
  -H "Authorization: Bearer <TOKEN>"
```

No navegador: abra a URL do Vercel, faça login, cadastre um registro, busque, exporte. Pronto.

> **Primeira chamada lenta?** No plano free o Render hiberna após inatividade; a primeira requisição
> "acorda" o serviço (~30 s). Normal.

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
- [ ] Pacote GHCR público **ou** _registry credential_ no Render
- [ ] `/actuator/health` responde `UP` e o login funciona
