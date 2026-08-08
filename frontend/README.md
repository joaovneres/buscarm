# BuscaRM — Frontend (Next.js)

Interface web do BuscaRM. Padrões em [`../docs/arquitetura.md`](../docs/arquitetura.md) (seção 3)
e skill `frontend-dev`.

## Requisitos
- Node 20+
- Backend rodando (ver `../backend`) para as buscas retornarem dados.

## Configuração
Copie `.env.example` para `.env.local` e ajuste `NEXT_PUBLIC_API_URL` (padrão `http://localhost:8080`).

## Scripts
```
npm install        # instala dependências
npm run dev        # ambiente de desenvolvimento (http://localhost:3000)
npm run build      # build de produção
npm run start      # sobe o build de produção
npm run lint       # ESLint (next/core-web-vitals)
npm run test       # testes unitários (Vitest + Testing Library)
npm run test:e2e   # testes ponta a ponta (Playwright)
```

## Estrutura (Feature-Sliced Design — ADR-0008)
Direção de dependência: `app → features → entities → shared`.
```
app/                                   # rotas (App Router): /, /registros
features/busca-registros/{ui,model}/   # BuscaRegistros, TabelaResultados + useBuscaRegistros
entities/registro/{model,api}/         # types (espelham DTOs) + buscarRegistros
shared/api/client.ts                   # cliente central de fetch (trata ApiError do backend)
tests/                                 # unit (Vitest) + e2e (Playwright)
```

## Fluxo atual (Épico 4 — parcial)
Tela `/registros`: busca por **nome** (parcial, acento-insensível) ou por **RM**, com destaque do
RM nos resultados e estados de loading/erro acessíveis. Consome `GET /api/registros` do backend.
