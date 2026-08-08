---
name: frontend-dev
description: Padrões de desenvolvimento do frontend BuscaRM (Next.js + React + TypeScript). Use ao criar ou alterar rotas, componentes, hooks, chamadas de API, telas de busca/importação ou testes do frontend. Garante estrutura feature-first, acessibilidade WCAG 2.2 e consumo padronizado da API.
---

# Frontend Dev — BuscaRM (Next.js + React + TS)

Referências: [`docs/arquitetura.md`](../../../docs/arquitetura.md) (seção 3) e os ADRs.
Siga também a skill `git-workflow` para commits/PR.

## Estrutura (Feature-Sliced — ADR-0008)

Direção de dependência única: `app → features → entities → shared`. Uma feature **não** importa outra.

```
app/            # rotas (App Router) — composição fina das telas
features/       # casos de uso interativos: <feature>/ui + <feature>/model (hooks)
entities/       # domínio do front: <entidade>/model (types) + <entidade>/api (acesso a dados)
shared/         # infra reutilizável: api/client, ui, config
tests/          # Vitest + Playwright
```

## Regras de código

- **TypeScript estrito.** Sem `any`; tipos dos dados espelham os DTOs do backend em `entities/*/model/types.ts`.
- **Server Components por padrão**; use `"use client"` só quando há estado/interação (upload, preview, edição).
- **Custom hooks** em `features/*/model` (`useBuscaRegistros`) encapsulam chamada à API + estado.
  Componentes (`features/*/ui`) não fazem `fetch` direto.
- **Acesso a dados** da entidade em `entities/*/api`, sobre o **API client centralizado**
  `shared/api/client.ts`: único ponto de `fetch`, injeta o JWT (`Authorization: Bearer`), trata erro
  no formato `ApiError`. Nunca chamar `fetch` solto nem importar entre features.
- **Container/Presentational**: separe componente que busca dados do que só renderiza.
- **Estado**: `useState`/`useReducer` local por padrão; Context só para auth/sessão. Sem lib de estado global sem necessidade.
- **Estados explícitos** em todo fluxo assíncrono: idle / loading / sucesso / erro (principalmente importação).
- **Estilo**: uma abordagem única (CSS Modules) — mantenha consistência; componentes de UI pequenos e reutilizáveis.
- **Sem segredos no código**; use `NEXT_PUBLIC_*` só para o que é realmente público (ex.: URL da API).

## Acessibilidade (requisito, WCAG 2.2)

- HTML **semântico** (`<button>`, `<label htmlFor>`, `<table>` com `<th scope>`), não `div` clicável.
- Foco visível, navegação por teclado, ordem lógica de tabulação.
- `aria-*` onde a semântica não basta (ex.: status de importação, mensagens de erro com `role="alert"`).
- Formulários: todo campo com label associado e erro descritivo.
- Antes do PR: rodar **axe-core** (sem violações críticas) e **Lighthouse** (acessibilidade alta).

## Fluxos-chave

- **Busca** (`registros`): input nome/RM → lista → **destaque do RM**. Empty state e loading claros.
- **Importação planilha**: upload → **pré-visualização** (linha, RM, nome, situação; seleciona válidos) → confirma → relatório.
- **Importação foto**: upload/captura → tabela do OCR com checkbox + **correção inline** → confirma.
  Nada é gravado sem confirmação humana.

## Testes (obrigatório na mesma PR)

- **Vitest + React Testing Library**: componentes/hooks — teste comportamento visível ao usuário, não implementação.
- **Playwright**: e2e do fluxo principal (planilha → busca → RM).

## Checklist de PR (frontend)

- [ ] Camadas respeitadas (`app → features → entities → shared`); sem import entre features
- [ ] Chamadas via `shared/api/client` a partir de `entities/*/api` (JWT + erro tratado)
- [ ] Estados idle/loading/sucesso/erro cobertos
- [ ] Acessibilidade: semântica, foco, labels; axe/Lighthouse sem violação crítica
- [ ] Tipos TS alinhados aos DTOs do backend
- [ ] Testes unit (e e2e se fluxo principal)
