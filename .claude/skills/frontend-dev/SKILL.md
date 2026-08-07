---
name: frontend-dev
description: Padrões de desenvolvimento do frontend BuscaRM (Next.js + React + TypeScript). Use ao criar ou alterar rotas, componentes, hooks, chamadas de API, telas de busca/importação ou testes do frontend. Garante estrutura feature-first, acessibilidade WCAG 2.2 e consumo padronizado da API.
---

# Frontend Dev — BuscaRM (Next.js + React + TS)

Referências: [`docs/arquitetura.md`](../../../docs/arquitetura.md) (seção 3) e os ADRs.
Siga também a skill `git-workflow` para commits/PR.

## Estrutura (App Router, feature-first)

```
app/            # rotas: (auth)/login, registros, importacao/planilha, importacao/imagem
features/       # lógica por domínio: { components, hooks, api, types } (NÃO dentro de app/)
components/ui/  # UI reutilizável e acessível
lib/            # api client, auth, format, utils
tests/          # Vitest + Playwright
```

Regra: rotas ficam finas em `app/`; a lógica de domínio mora em `features/`.

## Regras de código

- **TypeScript estrito.** Sem `any`; tipos dos dados espelham os DTOs do backend em `features/*/types.ts`.
- **Server Components por padrão**; use `"use client"` só quando há estado/interação (upload, preview, edição).
- **Custom hooks** (`useRegistros`, `useImportacaoPlanilha`) encapsulam chamada à API + estado.
  Componentes não fazem `fetch` direto.
- **API client centralizado** em `lib/api.ts`: único ponto de `fetch`, injeta o JWT
  (`Authorization: Bearer`), trata erro no formato `ApiError` do backend. Nunca chamar `fetch` solto.
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

- [ ] Lógica em `features/`/hooks; rota fina em `app/`
- [ ] Chamadas via `lib/api` (JWT + erro tratado)
- [ ] Estados idle/loading/sucesso/erro cobertos
- [ ] Acessibilidade: semântica, foco, labels; axe/Lighthouse sem violação crítica
- [ ] Tipos TS alinhados aos DTOs do backend
- [ ] Testes unit (e e2e se fluxo principal)
