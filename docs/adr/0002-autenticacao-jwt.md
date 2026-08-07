# ADR-0002: Autenticação com JWT stateless

- **Status:** Aceito
- **Data:** 2026-08-07
- **Contexto de decisão:** Épico 3 — autenticação e autorização

## Contexto

O frontend (Vercel) e o backend (Render) estão em domínios diferentes. A secretaria precisa
autenticar para cadastrar, importar e alterar registros. Precisamos de um mecanismo simples de
operar em deploy distribuído e fácil de proteger endpoints por papel.

## Decisão

Usar **JWT stateless** com Spring Security:

- Login (`/api/auth/login`) valida credenciais (senha em **BCrypt**) e devolve um **JWT** assinado.
- O frontend envia o token em `Authorization: Bearer <jwt>` a cada requisição.
- Um filtro no backend valida o token e popula o `SecurityContext`; autorização por papéis
  (`SECRETARIA`, `ADMIN`).

## Alternativas consideradas

- **Sessão server-side (cookie):** exige store de sessão e configuração de cookies cross-site
  (SameSite/CORS) entre Vercel e Render; mais atrito no deploy desacoplado. Descartado para o MVP.
- **OAuth/provedor externo:** overhead desnecessário para um público interno pequeno. Descartado.

## Consequências

- Positivas: sem estado no servidor, escala e combina com deploy separado; simples de proteger endpoints.
- Negativas: revogação de token é mais difícil (mitigar com expiração curta e, se preciso, refresh token);
  o segredo de assinatura deve ficar em variável de ambiente e nunca no repositório.
