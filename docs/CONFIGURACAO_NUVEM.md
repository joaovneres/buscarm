# Configuração de nuvem — credenciais e segredos

Mapa do que cada serviço precisa, **quando** precisa e **onde** o segredo mora. Regra geral:
segredo nunca vai para o repositório — vai para *secrets* do GitHub/Render/Vercel ou `.env` local
(ignorado pelo git).

> **Ordem recomendada:** primeiro fazer o *scaffold* do backend (Épico 2) e do frontend
> (Épico 4). Só então criar Neon/Render/Vercel e ligar o deploy — antes disso não há aplicação
> para implantar, e guardar tokens agora não adianta nada.

## O que precisamos, por serviço

| Serviço | Credencial / segredo | Onde mora | Quando é preciso |
| --- | --- | --- | --- |
| **GitHub** | login `gh` | ambiente | ✅ já configurado |
| **Neon (Postgres)** | `DATABASE_URL` (connection string, contém senha) | `.env` local do backend + env do Render | ao rodar o backend (Épico 2) |
| **Backend / JWT** | `JWT_SECRET` (aleatório forte) | env local + Render | Épico 3 (posso gerar) |
| **Backend / CORS** | `CORS_ALLOWED_ORIGINS` (URL do Vercel) | env do Render | ao publicar |
| **Frontend** | `NEXT_PUBLIC_API_URL` (URL do backend no Render) | `frontend/vercel.json` (`build.env`) + env do Vercel | ao publicar |
| **GHCR (imagem)** | `GITHUB_TOKEN` embutido no Actions | automático | release.yml (artefato) |

Observações:
- **Deploy é automático a partir do Git** (ADR-0005): Render (serviço Docker ligado ao repo) e
  Vercel (projeto ligado ao repo) publicam a cada push na `main`. **Não há deploy hooks nem
  secrets de deploy** no GitHub Actions — o gatilho é o próprio push.
- O Render **builda o `backend/Dockerfile` a partir do repo**, então não puxa a imagem do GHCR e
  não precisa de credencial de registry. A imagem do `release.yml` é só artefato de rastreabilidade.
- **Tesseract/OCR** é dependência de sistema no contêiner do backend (instalada no Dockerfile),
  não é token.

## Não é preciso token de API para o dia a dia

Com o deploy automático, **não** precisamos de tokens de API de Vercel/Render/Neon no fluxo normal.
Basta o runtime:
1. `DATABASE_URL` do Neon (env do Render),
2. `JWT_SECRET` e `ADMIN_*` (env do Render),
3. `NEXT_PUBLIC_API_URL` (já fixado em `frontend/vercel.json`).

Os tokens de API (guardados em `.deploy-secrets.env`, **fora do git**) só servem para operar/inspecionar
os serviços por linha de comando — opcional e mais sensível.

## Como fornecer com segurança

- Variáveis de runtime (`DATABASE_URL`, `JWT_SECRET`, `ADMIN_*`, `CORS_ALLOWED_ORIGINS`) vão nos
  painéis do **Render** (backend) e **Vercel** (frontend), **não** em *secrets* do Actions.
- **Prefira não colar segredos no chat** (ficam no histórico); defina-os direto nos painéis.
- Se um segredo for colado por engano em algum lugar versionado, **rotacione** (gere de novo).
