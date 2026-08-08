# BuscaRM — Arquitetura e Padrões de Design

Este documento define **como** o BuscaRM é construído: a arquitetura de alto nível, e os
padrões de design adotados no backend e no frontend. Decisões pontuais ficam registradas como
[ADRs](./adr/). Skills de desenvolvimento reforçam estes padrões no dia a dia
(`.claude/skills/`).

---

## 1. Visão geral

```
        ┌──────────────────────┐        REST/HTTPS (JSON)      ┌──────────────────────┐
        │   Frontend Next.js   │  ───────────────────────────▶ │   Backend Spring Boot│
        │   React + TS (Vercel)│  ◀─────────────────────────── │   Java (Render)      │
        └──────────────────────┘        JWT no Authorization   └──────────┬───────────┘
                                                                           │ JPA
                                                                ┌──────────▼───────────┐
                                                                │  PostgreSQL (Neon)   │
                                                                └──────────────────────┘
```

Princípios que valem para os dois lados:

1. **Separação de responsabilidades** por camadas/features.
2. **Pipeline de importação único** — Excel e OCR convergem para o mesmo `RegistroDTO` e o
   mesmo validador. Nunca duplicar regra de negócio.
3. **Nada de escrita silenciosa** — importação sempre validar → pré-visualizar → confirmar.
4. **Tipagem forte e contratos explícitos** — DTOs no back, tipos TS no front, OpenAPI como fonte.
5. **Testável por design** — dependências injetadas, serviços puros, sem lógica em controllers/componentes.

---

## 2. Backend (Spring Boot) — Arquitetura Hexagonal

Padrão adotado: **Hexagonal / Ports & Adapters** (ver [ADR-0007](./adr/0007-backend-hexagonal.md)).
O domínio e a aplicação não conhecem framework; adaptadores na borda implementam/consomem portas.

### 2.1 As camadas e a regra da dependência

```
        adapter/in/web  ──▶  port/in ─┐
        (REST, DTOs)                  │
                                      ▼
                            application (casos de uso)  ──▶  domain (regras puras)
                                      │
                                      ▼
        port/out ◀── adapter/out/persistence (JPA)
                 ◀── adapter (segurança: usuário da sessão)
```

- **domain**: modelo puro + regras (`Registro`, `NomeNormalizer`, exceptions). Sem Spring/JPA.
- **application**: casos de uso (implementam `port/in`) orquestrando `port/out`. Sem framework de infra.
- **port/in**: o que o mundo pode pedir ao sistema (`CadastrarRegistroUseCase`, `BuscarRegistrosUseCase`).
- **port/out**: o que o sistema precisa da infra (`RegistroRepositoryPort`, `UsuarioAtualPort`, `UsuarioSessaoPort`).
- **adapter/in/web**: `RestController` + DTOs (traduz HTTP ↔ comandos/domínio).
- **adapter/out/persistence**: entidade JPA + Spring Data + adaptador que implementa a porta (mapeia domínio ↔ entidade).

As dependências apontam **para dentro**: adaptadores → aplicação → domínio. O domínio nunca importa infra.

### 2.2 Organização por pacotes (feature + hexagonal)

```
com.buscarm
├── registro
│   ├── domain/                    # Registro, NomeNormalizer, exception/
│   ├── application/
│   │   ├── port/in/               # CadastrarRegistroUseCase, BuscarRegistrosUseCase
│   │   ├── port/out/              # RegistroRepositoryPort, UsuarioAtualPort, UsuarioSessaoPort
│   │   └── RegistroApplicationService
│   └── adapter/
│       ├── in/web/                # RegistroController + dto/
│       └── out/persistence/       # RegistroJpaEntity, SpringData, Adapter, Mapper
├── importacao/  (Épicos 5 e 7 — mesma forma: OCR/POI são adapters da mesma porta)
├── seguranca/                     # SecurityConfig + adapters de UsuarioAtualPort/UsuarioSessaoPort
├── shared/                        # ApiError, Pagina, web/GlobalExceptionHandler
└── config/                        # JpaAuditingConfig
```

### 2.3 Padrões de design aplicados

| Padrão | Onde | Por quê |
| --- | --- | --- |
| **Hexagonal (Ports & Adapters)** | todo o back | isola domínio de framework/infra; testável |
| **Ports** (interfaces in/out) | `application/port` | inverte dependências; adaptadores plugáveis |
| **Adapter** | web, persistência, segurança | tradução entre borda e aplicação |
| **DTO/Command + Mapper** | web ↔ aplicação ↔ domínio | não vaza entidade JPA nem tipo web para dentro |
| **Strategy** | extração de origem (`POI` × `OCR`) para a mesma porta | mesma regra, fontes diferentes |
| **Pipeline** | importação: extrair → validar → pré-visualizar → confirmar | reuso entre Excel e OCR |
| **Dependency Injection** | Spring (construtor) nos adaptadores/serviço | testável, sem `new` acoplado |
| **Global Exception Handler** | `@RestControllerAdvice` (shared/web) | erros consistentes (`ApiError`) |
| **Soft delete** | `ativo=false` em vez de DELETE | preserva integridade e auditoria |

### 2.4 Contratos e erros

- Respostas de erro padronizadas em um `ApiError { timestamp, status, code, message, path, fieldErrors[] }`.
- Validação de entrada com Bean Validation (`@Valid`, `@NotBlank`, etc.).
- Nunca engolir exceção; mapear para status HTTP adequado no handler global.

### 2.5 Segurança

- **JWT stateless** (ADR-0002). Filtro de autenticação valida o token e popula o `SecurityContext`.
- Senhas com **BCrypt**. Papéis `SECRETARIA` e `ADMIN` controlam acesso por endpoint.
- CORS restrito à origem do frontend (Vercel/local).

### 2.6 Banco e migrations

- **Flyway** versiona o schema (`V1__...sql`, `V2__...sql`). Nunca alterar migration já aplicada.
- Índices: `rm` único; índice para busca por nome normalizado (ver 2.7).

### 2.7 Normalização de nome (regra central de busca)

- Guardar `nome_completo` como digitado **e** um campo/coluna normalizada (minúsculas, sem
  acentos, formato `sobrenome, nome`) para busca acento/caixa-insensível.
- Busca por nome = `LIKE`/`ILIKE` sobre o campo normalizado; futura evolução para busca aproximada.

---

## 3. Frontend (Next.js + React + TypeScript) — Feature-Sliced Design

Padrão adotado: **modular por camadas orientado a features (Feature-Sliced)**, **não Microfrontend**
(ver [ADR-0008](./adr/0008-frontend-feature-sliced.md)). Camadas com direção de dependência única:
`app → features → entities → shared`.

### 3.1 Estrutura (App Router + Feature-Sliced)

```
frontend/
├── app/                          # rotas (App Router) — composição das telas
│   ├── registros/  page.tsx
│   └── layout.tsx  page.tsx  globals.css
├── features/                     # casos de uso interativos
│   └── busca-registros/
│       ├── ui/       # BuscaRegistros, TabelaResultados
│       └── model/    # useBuscaRegistros (hook)
├── entities/                     # modelos de domínio do front
│   └── registro/
│       ├── model/    # types (espelham DTOs)
│       └── api/      # buscarRegistros (acesso a dados)
├── shared/                       # infra reutilizável
│   └── api/client.ts # único ponto de fetch (trata ApiError, injeta JWT)
└── tests/                        # unit (Vitest) + e2e (Playwright)
```

Regra de ouro: a camada de cima só importa das de baixo; uma feature **não** importa outra feature.
Importação (Excel/foto) entra como novas `features/` reusando `entities/registro` e `shared/api`.

### 3.2 Padrões de design aplicados

| Padrão | Onde | Por quê |
| --- | --- | --- |
| **Feature-Sliced (app/features/entities/shared)** | todo o front | coesão por domínio + limites de dependência explícitos |
| **Container/Presentational** | `ui/` (dados × apresentação) | separa fetch/estado de renderização |
| **Custom hooks** | `features/*/model` (`useBuscaRegistros`) | encapsula chamada à API + estado |
| **API client centralizado** | `shared/api/client.ts` | único ponto de `fetch`, injeta JWT, trata erro |
| **Server vs Client Components** | App Router | server por padrão; `"use client"` só com interação/estado |
| **Tipos por entidade** | `entities/*/model/types.ts` | espelham os DTOs do backend |

### 3.3 Dados e estado

- **Data fetching:** Server Components para leitura quando possível; para fluxos interativos
  (upload, preview, edição) usar Client Components + hook (`features/*/model`) sobre `shared/api/client`.
- **Estado:** local (`useState`/`useReducer`) por padrão; contexto só para auth/sessão. Evitar
  biblioteca de estado global sem necessidade.
- **Erros e loading:** todo fluxo de importação tem estados explícitos (idle/loading/sucesso/erro).

### 3.4 Acessibilidade (requisito, não enfeite)

- HTML semântico, labels associados, foco visível, navegação por teclado.
- Tabelas de pré-visualização com `<th scope>`, `aria-*` onde necessário.
- Metas: **axe-core** sem violações críticas e **Lighthouse** de acessibilidade alto (ver Épico 8).

### 3.5 Estilo e componentes

- Componentes de UI pequenos, reutilizáveis e acessíveis em `shared/ui/`.
- CSS Modules ou solução única definida no início — manter consistência (uma abordagem só).

---

## 4. Contrato Front ↔ Back

- **OpenAPI/Swagger** é a fonte da verdade dos endpoints. Tipos TS do front espelham os DTOs.
- Autenticação: header `Authorization: Bearer <jwt>`.
- Padrão de resposta de erro idêntico ao `ApiError` do backend, consumido pelo `lib/api`.

## 5. Onde cada requisito "mora"

| Requisito | Backend | Frontend |
| --- | --- | --- |
| Pesquisa por nome/RM | `registro` (service + query normalizada) | `features/registros` |
| Cadastro/alteração/desativação | `registro` (+auditoria) | `features/registros` |
| Importação Excel | `importacao/planilha` + validador | `features/importacao/planilha` |
| Importação foto/OCR | `importacao/imagem` + validador | `features/importacao/imagem` |
| Autenticação | `usuario` + `seguranca` (JWT) | `(auth)/login` + auth no `lib/api` |
| Auditoria | `auditoria` | tela admin |
| Exportação/backup | `registro`/`importacao` (export) | botão/tela export |

---

Referências de decisão: ver [`adr/`](./adr/). Padrões operacionais (commits, PRs, código):
ver as skills em [`.claude/skills/`](../.claude/skills/).
