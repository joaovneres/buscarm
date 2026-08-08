# BuscaRM — Backend (Spring Boot)

API REST do BuscaRM. Padrões em [`../docs/arquitetura.md`](../docs/arquitetura.md) (seção 2) e
skill `backend-dev`.

## Requisitos
- Java 21
- Maven 3.9+
- PostgreSQL (local ou Neon) para rodar a aplicação; os **testes** usam H2 em memória.

## Rodar os testes
```
mvn -B verify
```
Os testes de integração usam o profile `test` (H2, sem Flyway) — não precisam de banco externo.

## Rodar a aplicação
Defina as variáveis (veja `.env.example`) e execute:
```
export DATABASE_URL="jdbc:postgresql://localhost:5432/buscarm"
export DB_USER="buscarm"
export DB_PASSWORD="buscarm"
mvn spring-boot:run
```
- API: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

## Autenticação (Épico 3 — JWT)
- `POST /api/auth/login` (público) → `{ token, nome, email, papel }`. Envie o token nas demais
  chamadas em `Authorization: Bearer <token>`. Sem token → 401.
- Papéis `SECRETARIA`/`ADMIN`; `/api/auditoria/**` exige ADMIN. Senhas em BCrypt.
- **Admin inicial** criado no 1º start se não houver usuários (`buscarm.admin.email`/`.senha`,
  padrão `admin@buscarm.local`/`admin12345` — **troque em produção**).

## Endpoints (Épico 4 — parcial)
| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/registros` | Cadastro manual (valida RM único) |
| `GET` | `/api/registros?nome=` | Pesquisa por nome (parcial, acento-insensível) |
| `GET` | `/api/registros?rm=` | Pesquisa por RM |
| `GET` | `/api/registros/{id}` | Busca por id |
| `GET` | `/api/registros/exportar?formato=xlsx\|csv` | Exporta todos os ativos (download) — Épico 6 |
| `GET` | `/api/auditoria?pagina=&tamanho=` | Histórico de ações sensíveis — **ADMIN** (Épico 9) |

Paginação: `?pagina=0&tamanho=20` (ordena por sobrenome).

### Importação por planilha (Épico 5)
| Método | Rota | Descrição |
| --- | --- | --- |
| `POST` | `/api/importacoes/planilha/validar` | Upload `.xlsx` (multipart `arquivo`) → pré-visualização classificada (**não grava**) |
| `POST` | `/api/importacoes/planilha/confirmar` | `{importacaoId, linhasAprovadas[]}` → grava só os válidos aprovados + relatório |
| `GET` | `/api/importacoes/{id}` | Status/relatório da importação |

Cada linha é classificada em `VALIDO`/`INVALIDO`/`DUPLICADO`/`CONFLITO`; a gravação reusa o
cadastro (`CadastrarRegistroUseCase`) e tudo fica rastreado em `importacao`/`importacao_item`.

## Estrutura (Arquitetura Hexagonal — ADR-0007)
```
com.buscarm
├── registro
│   ├── domain/                 # Registro, NomeNormalizer, exception/ (regras puras)
│   ├── application/
│   │   ├── port/in/            # *UseCase (portas de entrada)
│   │   ├── port/out/           # *Port (portas de saída)
│   │   └── RegistroApplicationService
│   └── adapter/
│       ├── in/web/             # RegistroController + dto/
│       └── out/persistence/    # RegistroJpaEntity, SpringData, Adapter, Mapper
├── seguranca/                  # SecurityConfig + adapters de usuário (JWT no Épico 3)
├── shared/                     # ApiError, Pagina, web/GlobalExceptionHandler
├── config/                     # JpaAuditingConfig
└── db/migration (Flyway)
```
O domínio/aplicação não dependem de Spring/JPA — testáveis com mocks das portas
(`RegistroApplicationServiceTest`).

> Segurança está **temporariamente aberta** (permitAll) até o Épico 3 (JWT — ADR-0002).

## Auditoria de autoria (ADR-0006)
Cada registro guarda `criado_por` e `atualizado_por` pelo **usuário da sessão**:
- **Aplicação:** JPA Auditing (`@CreatedBy`/`@LastModifiedBy`) via `UsuarioAtualProvider`
  (hoje `sistema`; vira o usuário do JWT no Épico 3).
- **Banco (Postgres):** a app define a GUC `app.current_username` por transação e um **trigger**
  (`V2`) grava/valida a autoria, bloqueando gravação sem usuário de sessão. Nos testes (H2) usa-se
  um no-op e a autoria vem do JPA Auditing.
