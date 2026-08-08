---
name: backend-dev
description: Padrões de desenvolvimento do backend BuscaRM (Java + Spring Boot). Use ao criar ou alterar controllers, services, repositories, entidades, DTOs, migrations Flyway, segurança ou testes do backend. Garante arquitetura em camadas, package-by-feature e o pipeline de importação único.
---

# Backend Dev — BuscaRM (Spring Boot)

Referências: [`docs/arquitetura.md`](../../../docs/arquitetura.md) (seção 2) e os ADRs.
Siga também a skill `git-workflow` para commits/PR.

## Arquitetura Hexagonal / Ports & Adapters (respeite sempre — ADR-0007)

Dependências apontam **para dentro**: adapter → application → domain. O domínio nunca importa infra.

```
adapter/in/web → port/in → application → domain
                                   ↓
                               port/out ← adapter/out/persistence (JPA)
                                        ← adapter (segurança)
```

- **domain**: modelo puro + regras (ex.: `Registro`, `NomeNormalizer`, `exception/`). **Sem Spring/JPA.**
- **application**: casos de uso implementam `port/in`, orquestram `port/out`. `@Service`,
  `@Transactional`. É aqui que os testes unitários (com mocks das portas) vivem — sem contexto Spring.
- **port/in**: interfaces do que o sistema oferece (`*UseCase`). Entrada por *command* (record), não DTO web.
- **port/out**: interfaces do que o sistema precisa (`*RepositoryPort`, etc.).
- **adapter/in/web**: `@RestController` + DTOs. Só HTTP; traduz DTO ↔ command/domínio. **Zero regra.**
- **adapter/out/persistence**: `@Entity` JPA + Spring Data + adaptador que implementa a porta
  (mapeia domínio ↔ entidade). **Nunca** serialize a entidade na API nem vaze `Page`/tipos Spring para dentro.

## Organização (feature + hexagonal)

```
com.buscarm
├── <feature>/domain
├── <feature>/application/{port/in, port/out, *ApplicationService}
├── <feature>/adapter/{in/web, out/persistence}
├── seguranca/   (adapters de UsuarioAtualPort/UsuarioSessaoPort + SecurityConfig)
├── shared/      (ApiError, Pagina, web/GlobalExceptionHandler)
└── config/
```

`importacao` (Épicos 5 e 7) segue a mesma forma: `planilha/` (Apache POI) e `imagem/` (Tess4J/OCR)
são **adaptadores de entrada** que convergem para a **mesma porta**/validação (ADR-0003) — nunca
duplique regra de validação/duplicidade.

## Regras de código

- **Injeção por construtor** (final fields), nunca `@Autowired` em campo.
- **DTOs de entrada e saída** separados das entidades; conversão em Mapper dedicado.
- **Bean Validation** (`@NotBlank`, `@NotNull`, etc.) nos DTOs de entrada.
- **Erros** via `@ControllerAdvice` global → `ApiError { timestamp, status, code, message, path, fieldErrors }`.
  Nunca engula exceção nem retorne stacktrace.
- **Nomes em português** no domínio (Registro, Importacao, Validador…), coerentes com o negócio.
- **Sem lógica em controller/entity.** Regra vai para o service.
- **Soft delete**: desativar (`ativo=false`), não apagar.
- **Auditoria**: toda ação sensível (criar/alterar/desativar/importar) registra em `auditoria`.

## Banco e migrations (Flyway)

- Schema só muda por migration `V<n>__descricao.sql`. **Nunca** edite uma migration já aplicada — crie a próxima.
- `rm` é único; crie índice para busca por nome normalizado.
- Normalização de nome (minúsculo, sem acento, `sobrenome, nome`) para busca acento/caixa-insensível.

## Segurança

- JWT stateless (ADR-0002); senha em BCrypt; papéis `SECRETARIA`/`ADMIN` protegem endpoints.
- Segredos só em variáveis de ambiente. CORS restrito à origem do frontend.

## Importação — endpoints padrão

```
POST /api/importacoes/planilha/validar    # NÃO grava, retorna resumo + itens classificados
POST /api/importacoes/planilha/confirmar   # grava só os aprovados + relatório
POST /api/importacoes/imagem/processar
POST /api/importacoes/imagem/confirmar
GET  /api/importacoes/{id}
```

`validar` nunca persiste registros definitivos. Persistir sempre com `importacao` + `importacao_item`.

## Testes (obrigatório na mesma PR)

- **Unit (JUnit 5 + Mockito)**: services, validador, leitor de planilha, normalização. Cubra os
  quatro status (VALIDO/INVALIDO/DUPLICADO/CONFLITO).
- **Integração (Spring Boot Test)**: endpoints de busca e importação.
- Teste comportamento e casos de borda, não implementação.

## Checklist de PR (backend)

- [ ] Hexagonal respeitado (domínio/aplicação sem Spring/JPA; regra fora do controller)
- [ ] Portas usadas; adaptador implementa a porta; entidade JPA e `Page` não vazam para dentro
- [ ] Validação e tratamento de erro global
- [ ] Migration nova (se schema mudou) — nunca editar aplicada
- [ ] Testes unit/integração cobrindo o caso e as bordas
- [ ] Sem segredos no código; OpenAPI atualizado
