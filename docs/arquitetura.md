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

## 2. Backend (Spring Boot)

### 2.1 Arquitetura em camadas

```
Controller  →  Service  →  Repository  →  Entity/DB
   (Web)       (regras)     (Spring Data)   (JPA)
     │            │
     ▼            ▼
   DTO  ◀──────  Mapper
```

- **Controller** (`*Controller`): só HTTP. Recebe/valida DTO de entrada, chama service, devolve
  DTO/`ResponseEntity`. Zero regra de negócio.
- **Service** (`*Service`): regra de negócio, transações (`@Transactional`), orquestração. É onde
  os testes unitários concentram.
- **Repository** (`*Repository`): interface Spring Data JPA. Queries derivadas ou `@Query`.
- **Entity** (`*`): mapeamento JPA. Não sai da camada de persistência — nunca serializar entidade direto.
- **DTO** (`*Request`/`*Response`): contrato da API. Conversão via **Mapper** dedicado.

### 2.2 Organização por pacotes (package-by-feature)

```
com.buscarm
├── registro         # cadastro/pesquisa de registros de matrícula
│   ├── RegistroController
│   ├── RegistroService
│   ├── RegistroRepository
│   ├── Registro (entity)
│   └── dto/  mapper/
├── importacao       # pipeline compartilhado Excel + OCR
│   ├── planilha/    # LeitorPlanilhaService (Apache POI)
│   ├── imagem/      # OCRService (Tess4J) + ExtratorRegistroService
│   ├── ValidadorRegistroService   # regra única de validação/duplicidade/conflito
│   ├── ImportacaoController
│   └── dto/ (RegistroDTO comum)
├── usuario          # autenticação/usuários
├── auditoria        # log de ações sensíveis
├── seguranca        # config Spring Security, JWT, CORS
└── comum            # exceptions, ApiError, GlobalExceptionHandler, config
```

### 2.3 Padrões de design aplicados

| Padrão | Onde | Por quê |
| --- | --- | --- |
| **Layered architecture** | todo o back | separação clara, testabilidade |
| **Repository** | Spring Data JPA | abstrai persistência |
| **DTO + Mapper** | fronteira da API | não vazar entidade; contrato estável |
| **Strategy** | leitura de origem (`LeitorPlanilha` × `ExtratorOCR`) | mesma saída (`RegistroDTO`), fontes diferentes |
| **Pipeline** | importação: extrair → normalizar → validar → pré-visualizar → confirmar | reuso entre Excel e OCR |
| **Dependency Injection** | Spring (construtor) | testável, sem `new` acoplado |
| **Global Exception Handler** | `@ControllerAdvice` | erros consistentes (`ApiError`) |
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

## 3. Frontend (Next.js + React + TypeScript)

### 3.1 Estrutura (App Router, feature-first)

```
frontend/
├── app/                      # rotas (App Router)
│   ├── (auth)/login/
│   ├── registros/            # pesquisa, cadastro, edição
│   ├── importacao/
│   │   ├── planilha/
│   │   └── imagem/
│   └── layout.tsx
├── features/                 # lógica por domínio (não em app/)
│   ├── registros/  { components, hooks, api, types }
│   └── importacao/ { components, hooks, api, types }
├── components/ui/            # componentes de UI reutilizáveis, acessíveis
├── lib/                      # api client (fetch), auth, format, utils
├── styles/
└── tests/                    # unit (Vitest) + e2e (Playwright)
```

### 3.2 Padrões de design aplicados

| Padrão | Onde | Por quê |
| --- | --- | --- |
| **Feature-based modules** | `features/` | coesão por domínio, escala melhor que por tipo |
| **Container/Presentational** | components de dados × de UI | separa fetch/estado de renderização |
| **Custom hooks** | `useRegistros`, `useImportacaoPlanilha` | encapsula chamada à API + estado |
| **API client centralizado** | `lib/api.ts` | único ponto de `fetch`, injeta JWT, trata erro |
| **Server vs Client Components** | App Router | render no servidor por padrão; `"use client"` só quando há interação/estado |
| **Tipos compartilhados** | `features/*/types.ts` | espelham os DTOs do backend |

### 3.3 Dados e estado

- **Data fetching:** Server Components para leitura quando possível; para fluxos interativos
  (upload, preview, edição) usar Client Components + hook chamando `lib/api`.
- **Estado:** local (`useState`/`useReducer`) por padrão; contexto só para auth/sessão. Evitar
  biblioteca de estado global sem necessidade.
- **Erros e loading:** todo fluxo de importação tem estados explícitos (idle/loading/sucesso/erro).

### 3.4 Acessibilidade (requisito, não enfeite)

- HTML semântico, labels associados, foco visível, navegação por teclado.
- Tabelas de pré-visualização com `<th scope>`, `aria-*` onde necessário.
- Metas: **axe-core** sem violações críticas e **Lighthouse** de acessibilidade alto (ver Épico 8).

### 3.5 Estilo e componentes

- Componentes de UI pequenos, reutilizáveis e acessíveis em `components/ui/`.
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
