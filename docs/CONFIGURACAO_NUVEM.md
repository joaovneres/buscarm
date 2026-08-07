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
| **Frontend** | `NEXT_PUBLIC_API_URL` (URL do backend no Render) | env do Vercel | ao publicar |
| **Render (deploy)** | `RENDER_DEPLOY_HOOK_URL` | *secret* do GitHub Actions | Épico 11 (deploy) |
| **Vercel (deploy)** | `VERCEL_DEPLOY_HOOK_URL` | *secret* do GitHub Actions | Épico 11 (deploy) |
| **GHCR (imagem)** | `GITHUB_TOKEN` embutido no Actions | automático | release.yml |

Observações:
- **Deploy hooks** (Render/Vercel) são URLs criadas **por serviço/projeto** nos respectivos
  painéis — logo, o serviço precisa existir primeiro. São menos poderosos que um token de API
  (só disparam deploy), portanto mais seguros de guardar.
- Se a imagem no **GHCR** ficar privada, o Render precisa de credencial para puxá-la
  (PAT com `read:packages`) — alternativa: tornar o pacote público **ou** fazer o Render buildar
  o próprio Dockerfile a partir do repo (evita esse segredo).
- **Tesseract/OCR** é dependência de sistema no contêiner do backend, não é token.

## Não é preciso token de API completo (recomendação)

Para o nosso fluxo (deploy manual via hook), **não** precisamos dos tokens de API completos de
Vercel/Render/Neon. Precisamos apenas de:
1. `DATABASE_URL` do Neon,
2. os dois **deploy hooks** (Render e Vercel),
3. `JWT_SECRET` (gerado por nós).

Tokens de API completos só seriam úteis se quiséssemos **criar os serviços por linha de comando**
em vez de pelo painel — o que é opcional e mais sensível.

## Como fornecer com segurança

- **Prefira não colar segredos no chat** (ficam no histórico). Em vez disso, defina você mesmo,
  no terminal (prefixe com `!` aqui), depois que o repo/serviços existirem:

```
gh secret set RENDER_DEPLOY_HOOK_URL --repo joaovneres/buscarm
gh secret set VERCEL_DEPLOY_HOOK_URL --repo joaovneres/buscarm
```

- Variáveis de runtime (`DATABASE_URL`, `JWT_SECRET`, `NEXT_PUBLIC_API_URL`) vão nos painéis do
  **Render** (backend) e **Vercel** (frontend), não em *secrets* do Actions.
- Se um segredo for colado por engano em algum lugar versionado, **rotacione** (gere de novo).
