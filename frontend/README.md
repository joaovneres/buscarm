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
app/                                   # rotas: /, /registros, /importacao/planilha
features/busca-registros/{ui,model}/   # BuscaRegistros, TabelaResultados + useBuscaRegistros
features/importar-planilha/{ui,model}/ # Upload, Preview, Relatório + useImportarPlanilha
features/exportar-registros/ui/        # ExportarRegistros (download .xlsx/.csv)
entities/registro/{model,api}/         # types + buscarRegistros + urlExportacaoRegistros
entities/importacao/{model,api}/       # types + validarPlanilha / confirmarImportacao
shared/api/client.ts                   # fetch central (GET, POST JSON e multipart; trata ApiError)
tests/                                 # unit (Vitest) + e2e (Playwright)
```

Fluxo de importação (`/importacao/planilha`): upload `.xlsx` → **pré-visualização** classificada
(seleção só dos válidos) → **confirmação** → relatório. Nada grava sem confirmação humana.

## Fluxo atual (Épico 4 — parcial)
Tela `/registros`: busca por **nome** (parcial, acento-insensível) ou por **RM**, com destaque do
RM nos resultados e estados de loading/erro acessíveis. Consome `GET /api/registros` do backend.
