# BuscaRM — Resumo do que foi implementado

> Documento-síntese do estado atual do projeto: o que existe, como funciona e onde cada
> peça mora. Gerado a partir do código, das migrations, dos workflows de CI/CD e da
> documentação (`docs/`, `PLANO_DE_ACAO.md`, ADRs).
> **Data:** 2026-08-24.

---

## 1. O que é o sistema

O **BuscaRM** torna pesquisável o índice **Nome → RM** do livro de Registro de Matrícula de
uma escola. A secretaria digita o nome, o sistema devolve o **RM**, e com o RM localiza-se o
prontuário físico na gaveta. **Não** digitaliza o prontuário — digitaliza apenas o *índice*
para encontrá-lo.

**Motivadores levantados com a secretaria:** ~6.000 registros; busca hoje manual num livro
físico; integridade dos dados é crítica (a última atualização do livro perdeu nomes); busca
sempre por nome no formato `Sobrenome, Restante do nome`; existe uma planilha Excel com
milhares de registros para a carga inicial.

**Regra de ouro:** *nenhuma importação grava direto no cadastro*. Excel e foto passam sempre
por **validar → pré-visualizar → confirmar** com decisão humana; nunca sobrescreve
silenciosamente um registro existente.

---

## 2. Stack e arquitetura

| Camada | Tecnologias |
| --- | --- |
| Frontend | Next.js 14 (App Router) · React 18 · TypeScript · CSS · (Vercel) |
| Backend | Java 21 · Spring Boot 3.3 (Web, Data JPA, Security, Validation, Actuator) · Flyway · (Render) |
| Banco | PostgreSQL (Neon) — H2 nos testes |
| Importação | Apache POI 5.3 (Excel .xlsx) · parser CSV próprio (RFC 4180) · **binário nativo Tesseract** (OCR, via `ProcessBuilder`) |
| Auth | JWT stateless (jjwt 0.12) · BCrypt |
| API docs | springdoc-openapi / Swagger UI |
| Testes | JUnit 5 · Mockito · Spring Boot Test · Vitest · React Testing Library · Playwright |
| Infra | Docker · GitHub Actions (CI/CD) · GHCR (imagens versionadas) |

**Padrões arquiteturais**
- **Backend — Hexagonal (Ports & Adapters)** + organização *package-by-feature*
  (ADR-0007). Domínio e aplicação não conhecem framework; adaptadores na borda.
- **Frontend — Feature-Sliced Design** com direção de dependência única
  `app → features → entities → shared` (ADR-0008).
- **Pipeline de importação único** (ADR-0003): Excel e OCR convergem para o mesmo
  `RegistroExtraido` e o mesmo `ValidadorRegistro`. Regra de negócio nunca é duplicada.

```
Frontend Next.js (Vercel)  ──REST/HTTPS + JWT──▶  Backend Spring Boot (Render)  ──JPA──▶  PostgreSQL (Neon)
```

---

## 3. Backend — o que está implementado

Organizado por feature, cada uma com camadas `domain / application (port in+out) / adapter (in web + out persistence)`.

### 3.1 Registro (`com.buscarm.registro`) — cadastro, busca, exportação
- **Domínio:** `Registro`, `NomeNormalizer` (minúsculas, sem acentos, formato
  `sobrenome, nome` para busca acento/caixa-insensível), `FiltroRegistros`,
  `StatusValidacao`, exceptions (`RegistroDuplicado`, `RegistroNaoEncontrado`).
- **API REST:**
  - `POST /api/registros` — cadastro manual
  - `PUT /api/registros/{id}` — alteração (com auditoria)
  - `DELETE /api/registros/{id}` — **soft delete** (`ativo=false`, auditoria `DESATIVAR`)
  - `GET /api/registros?nome=` — busca parcial normalizada por nome
  - `GET /api/registros?rm=` — busca por RM
  - Filtros combináveis (`FiltroRegistros`): status de validação, ano e inicial do sobrenome
  - Paginação e ordenação por sobrenome
  - `GET /api/registros/exportar?formato=` — exportação **.xlsx / .csv**
- **Como funciona a busca:** guarda `nome_completo` como digitado **e** `nome_busca`
  normalizado; a consulta usa `LIKE`/`ILIKE` sobre `nome_busca` (índice dedicado).

### 3.2 Importação (`com.buscarm.importacao`) — coração do MVP + OCR
Pipeline **único** para as duas origens:
- **Portas de entrada:** `ValidarPlanilhaUseCase`, `ConfirmarImportacaoUseCase`,
  `ProcessarImagemUseCase`, `ConfirmarImagemUseCase`, `ConsultarImportacaoUseCase`.
- **Portas de saída (adaptáveis/Strategy):** `LeitorPlanilhaPort` → `LeitorPlanilhaAdapter`, que
  despacha por formato — **.xlsx** (Apache POI) e **.csv** (parser RFC 4180 com detecção de
  delimitador `;`/`,`/tab, aspas para nomes `Sobrenome, Nome` e BOM UTF-8); `ExtratorImagemPort` →
  `TesseractCliExtratorAdapter` (invoca o binário nativo `tesseract`, `--psm 3` auto + `--oem 1`
  LSTM) + `ExtratorImagemStub` (nos testes, `@Profile("!test")`).
- **Domínio compartilhado:** `RegistroExtraido`, `ValidadorRegistro` (classifica em
  **VÁLIDO / INVÁLIDO / DUPLICADO / CONFLITO** — CONFLITO = mesmo RM, nome diferente),
  `ExtratorLinhasOcr` (transforma linhas do OCR em `(RM, nome)`), `StatusImportacao`,
  `StatusItem`, `TipoImportacao`.
- **API REST:**
  - `POST /api/importacoes/planilha/validar` — lê **.xlsx ou .csv** e classifica; **não grava**, devolve resumo + itens
  - `POST /api/importacoes/planilha/confirmar` — efetiva **só os aprovados** + relatório
  - `POST /api/importacoes/imagem/processar` — OCR da foto
  - `POST /api/importacoes/imagem/confirmar` — efetiva os itens conferidos
  - `GET /api/importacoes/{id}` — status/relatório
- **Rastreabilidade:** cada importação persiste `importacao` + `importacao_item`
  (linha de origem, RM/nome extraídos, status, ligação ao registro criado).

### 3.3 Usuário e segurança (`usuario` + `seguranca`)
- **Auth JWT stateless** (ADR-0002): `JwtService`, `JwtAuthenticationFilter`, `SecurityConfig`.
- **Papéis (`Papel`):** `ADMIN` (CRUD de alunos + usuários + auditoria), `SECRETARIA` (CRUD de
  alunos) e `VISUALIZADOR` (somente leitura: consulta e exportação); controlam acesso por
  endpoint, com entry point 401.
- **Senhas** com **BCrypt** (`BCryptCodificadorSenhaAdapter`).
- **Endpoints:** `POST /api/auth/login`; gestão de usuários (`UsuarioController`: criar,
  alterar, resetar senha — admin); conta própria (`ContaController`: trocar senha).
- **Seed** de admin inicial (`UsuarioAdminSeeder`) + fluxo de **senha provisória** (V8).

### 3.4 Auditoria (`auditoria`)
- Registra ações sensíveis (CRIAR / ALTERAR / DESATIVAR / IMPORTAR / EXPORTAR) com usuário
  da sessão, entidade, id e detalhe.
- `GET /api/auditoria` (consulta admin). Autoria (`criado_por`/`atualizado_por`) também é
  garantida por **trigger** no banco (V2).

### 3.5 Transversais (`shared`, `config`)
- `GlobalExceptionHandler` (`@RestControllerAdvice`) → resposta de erro padronizada
  `ApiError { timestamp, status, code, message, path, fieldErrors[] }`.
- Bean Validation (`@Valid`) nos DTOs de entrada.
- `Pagina<T>` para respostas paginadas; JPA Auditing; **OpenAPI/Swagger** habilitado; CORS
  restrito à origem do frontend.

### 3.6 Banco de dados (Flyway — 8 migrations)
| Versão | Conteúdo |
| --- | --- |
| V1 | `registro_matricula` (RM único, `nome_busca` indexado, índice por sobrenome, autoria, soft delete) |
| V2 | Trigger de auditoria de autoria |
| V3 | `importacao` + `importacao_item` (rastreabilidade da origem) |
| V4 | `usuario` |
| V5 | `auditoria` |
| V6 | `status_validacao` em registro |
| V7 | migração de `email` → `username` |
| V8 | `senha_provisoria` (fluxo de troca obrigatória) |

> Regra: nunca alterar migration já aplicada — sempre uma nova `Vn`.

---

## 4. Frontend — o que está implementado

Estrutura **Feature-Sliced** (`app / features / entities / shared`). Cliente HTTP único em
`shared/api/client.ts` (injeta o JWT e trata o `ApiError`).

### 4.1 Rotas (`app/`)
- `/` — **é a tela de login** quando deslogado; logado, **redireciona direto para `/registros`**
  (não há mais home hub; o antigo `HomeNav`/feature `navegacao` foi removido).
- `/login` — autenticação
- `/registros` — busca, cadastro, edição, desativação, exportação e **atalho de importação**
  (bloco "Importar" no topo, ao lado de "Exportar")
- `/importacao/planilha` — importação por planilha (**.xlsx ou .csv**)
- `/importacao/imagem` — importação por foto (OCR)
- `/auditoria` — consulta de auditoria (admin)
- `/usuarios` — gestão de usuários (admin)
- `/trocar-senha` — troca de senha da própria conta

### 4.2 Features
- **auth** — `AuthContext`, `LoginForm`, `RequireAuth` (protege rotas), `AuthNav`.
- **busca-registros** — barra de busca, tabela/lista de resultados com destaque do RM,
  paginação, `StatusBadge`, modais de detalhes/cadastro/exclusão; hooks
  `useListaRegistros` e `useAcoesRegistro`.
- **importar-registros** — bloco compacto `ImportarRegistros` que leva às telas de importação
  (planilha/foto) a partir do topo de `/registros`.
- **importar-planilha** — upload (aceita **.xlsx e .csv**, com dica dos separadores),
  `PreviewImportacao` (seleção dos válidos), `RelatorioImportacao`; hook `useImportarPlanilha`.
- **importar-imagem** — upload/captura de foto, `ConferenciaOcr` (checkbox + correção
  inline dos resultados do OCR); hook `useImportarImagem`.
- **exportar-registros** — botão/tela de exportação (.xlsx/.csv).
- **consultar-auditoria** — tabela de auditoria; hook `useAuditoria`.
- **gestao-usuarios** — criar/editar usuário, reset de senha; hook `useGestaoUsuarios`.
- **conta** — trocar senha. **tema** — alternância de tema (claro/escuro).

### 4.3 Entities e shared
- `entities/{registro,importacao,usuario,auditoria}` — `model/types.ts` (espelham os DTOs) +
  `api/*` (funções de acesso a dados).
- `shared/`: `api/client.ts`, `auth/{roles,session}`, `theme/tema`, `ui/{Modal,icones}`.

### 4.4 Estados e acessibilidade
- Fluxos interativos com estados explícitos (idle / loading / sucesso / erro).
- Foco em WCAG 2.2: HTML semântico, labels, foco visível, navegação por teclado
  (auditorias axe-core/Lighthouse ainda pendentes — Épico 8).

---

## 5. Pipeline / CI/CD (GitHub Actions) — como funciona

Estratégia: **CI automática em todo PR** e **deploy automático a partir do Git** (ADR-0004 / ADR-0005).

### 5.1 `ci.yml` — em todo Pull Request e push na `main`
- **job `backend`** — Java 21 (Temurin), `mvn -B verify` (build + testes).
- **job `frontend`** — Node 20, `npm ci` → `lint` → `test` (Vitest) → `build`.
- **job `e2e`** — depende de backend+frontend; instala Playwright e roda `npm run test:e2e`.
- Os três jobs são os **status checks obrigatórios** para o merge na `main` (branch protegida,
  merge só via PR aprovado — regra em `.github/rulesets/main-protection.json`).

### 5.2 Deploy — automático a partir do Git (não há workflow de deploy)
- **Backend (Render):** serviço **Docker ligado ao repo** (`rootDir: backend`, builda
  `backend/Dockerfile`, `branch: main`, **`autoDeploy: on`**) → publica a cada push na `main`,
  em `https://buscarm.onrender.com`. **Não** usa a imagem do GHCR.
- **Frontend (Vercel):** projeto ligado ao repo → publica produção a cada push na `main` e gera
  preview por PR. `NEXT_PUBLIC_API_URL` fixado em `frontend/vercel.json`.
- Migrations **Flyway** rodam no start do backend.

### 5.3 `release.yml` — ao criar tag SemVer (`v*`)
- Publica **imagem Docker versionada no GHCR** (`buscarm-backend:vX.Y.Z` + `latest`) e o build do
  frontend como artifact — **artefatos versionados para rastreabilidade/rollback**, não a fonte do
  deploy. **Não faz deploy.**

### 5.4 `smoke.yml` — verificação pós-deploy (T11.7, `workflow_dispatch`)
- Confere `/actuator/health` do backend (tolerando o cold start do free tier) e, se informada, a
  URL do frontend. Rode em **Actions → Smoke (produção)** após publicar.

### 5.5 `keepalive.yml`
- Mantém o serviço do Render (plano free) acordado na janela útil (Seg–Sex, ~7h–19h BRT).

> A ADR-0005 previa originalmente um `deploy.yml` **manual** por deploy hooks/imagem GHCR; na
> prática o provisionamento seguiu por auto-deploy do Git, então esse workflow foi **removido**
> (ver "Atualização 2026-08-24" na ADR-0005).

**Configuração de nuvem:** `render.yaml` (referência de blueprint), `docs/deploy.md` (runbook
Neon → Render → Vercel), `docs/CONFIGURACAO_NUVEM.md` (segredos e onde moram).

---

## 6. Testes existentes

**Backend (JUnit 5 / Spring Boot Test, banco H2):**
`NomeNormalizerTest`, `RegistroApplicationServiceTest`, `RegistroIntegrationTest`,
`ExportacaoRegistrosIntegrationTest`, `ValidadorRegistroTest`, `ExtratorLinhasOcrTest`,
`ImportacaoPlanilhaIntegrationTest`, `ImportacaoImagemIntegrationTest`, `AuthIntegrationTest`,
`UsuarioAdminIntegrationTest`, `AuditoriaIntegrationTest`.

**Frontend:** unit em `tests/unit` (Vitest + RTL) e e2e em `tests/e2e` (Playwright).

---

## 7. Estado por épico (do `PLANO_DE_ACAO.md`)

| Épico | Estado | Observações |
| --- | --- | --- |
| 3 — Autenticação/autorização | ✅ Concluído | JWT, papéis, seed admin, BCrypt |
| 4 — Cadastro manual e pesquisa | ✅ Concluído | CRUD + busca nome/RM + telas |
| 5 — Importação de planilha | ✅ Quase completo | Agora **.xlsx e .csv**; atalho em `/registros`. Falta só T5.12 (amostra real com a secretaria) |
| 6 — Exportação e backup | ✅ Concluído | .xlsx/.csv + `scripts/backup.sh` + `docs/BACKUP.md` |
| 7 — OCR (foto) | ✅ Majoritário | Migrado para binário nativo `tesseract` (`--psm 3`), grande ganho de acerto numa página real (2/18 → 39/42). Falta pré-processamento OpenCV (T7.2) e medição real formal (T7.8) |
| 9 — Auditoria | ✅ Concluído | Registro + consulta + rastreabilidade da origem |
| 11 — DevOps/CI/CD | ✅ Concluído (essencial) | Nuvem provisionada e **deploy automático a partir do Git** funcionando: Render builda `backend/Dockerfile` e publica a cada push na `main` (`buscarm.onrender.com`, com keep-alive); Vercel publica o frontend. CI (backend/frontend/e2e) obrigatória por PR; `smoke.yml` para verificação pós-deploy. Pendências menores: token do Vercel com escopo do time (para operar por CLI) e medir/formalizar |
| 1 / 2 | ✅ Implementado na prática | Modelagem e backend base existem (checkboxes não marcados no plano) |
| 8 — Acessibilidade | ⬜ Pendente | Bases feitas; falta auditoria axe-core/Lighthouse formal |
| 10 — Testes (cobertura formal) | 🟡 Em andamento | Vários testes existem; falta meta de cobertura/relatório |
| 12 — Validação com a comunidade | ⬜ Pendente | Métricas e relatórios acadêmicos |

**Resumo:** o **fluxo-fim demonstrável** já existe de ponta a ponta —
`Planilha → Importação (validar/preview/confirmar) → Busca por nome → RM`, mais OCR por foto,
autenticação, auditoria e exportação. Está **no ar** com deploy automático (Render + Vercel). O que
falta é sobretudo **operacional** (rodar com dados reais da secretaria) e **acadêmico/qualidade**
(acessibilidade formal, métricas, relatórios).

---

## 8. Documentação de apoio no repositório

- `docs/arquitetura.md` — arquitetura e padrões de design (fonte principal).
- `docs/adr/` — 9 ADRs (stack, JWT, pipeline único, branches, CI/CD, auditoria, hexagonal,
  feature-sliced, OCR). Obs.: a ADR-0009 registrou a decisão original de OCR em Java com Tess4J;
  a implementação evoluiu depois para o **binário nativo `tesseract`** (ver §3.2) — a ADR ainda
  não foi revisada para refletir a troca.
- `docs/deploy.md`, `docs/CONFIGURACAO_NUVEM.md`, `docs/BACKUP.md`, `docs/REPOSITORIO.md`.
- `.claude/skills/` — padrões operacionais (backend-dev, frontend-dev, git-workflow, adr).
- `PLANO_DE_ACAO.md` — todo list por épicos/quinzenas.
