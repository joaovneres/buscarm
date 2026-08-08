# BuscaRM — Plano de Ação

> **Objetivo do sistema:** tornar pesquisável o índice **Nome → RM** do livro de Registro de
> Matrícula da escola, para que a secretaria localize rapidamente o número do RM e, com ele,
> o prontuário físico na gaveta. Não estamos digitalizando o prontuário — estamos digitalizando
> o *índice* necessário para encontrá-lo.

Fonte dos requisitos: `Requisitos/` (áudios/transcrições da secretaria), `planejamento.md`, `tec.md`.
Arquitetura e padrões: [`docs/arquitetura.md`](./docs/arquitetura.md). Decisões: [`docs/adr/`](./docs/adr/).

---

## Resumo do problema (levantado com a secretaria)

- Busca hoje é **manual** num livro físico: sobrenome → nome → número do RM → prontuário na gaveta.
- Mais de **6.000 registros** existentes (numeração sequencial e histórica).
- A última atualização do livro **perdeu muitos nomes** → integridade dos dados é crítica.
- A secretaria **só busca por nome** (o ano do registro é secundário/opcional).
- Formato do nome no livro: `Sobrenome, Restante do nome` — ex.: `Sousa, João Victor Cosme Neres de   203`.
- Existe uma **planilha Excel** com milhares de registros → carga inicial principal.

## Escopo do MVP

**Obrigatório:** autenticação · cadastro manual · pesquisa por nome · pesquisa por RM ·
alteração · desativação · **importação por Excel** · validação · duplicidades/conflitos ·
auditoria · exportação/backup · acessibilidade (WCAG 2.2) · testes · deploy em nuvem.

**Segunda etapa:** importação por **fotografia** (OCR/Tesseract) · pré-visualização · correção
humana · confirmação · análise de desempenho do reconhecimento.

**Regra de ouro:** *nenhuma importação grava direto no cadastro definitivo* — Excel e foto
passam por pré-visualização, validação e confirmação humana. Uma importação **nunca**
sobrescreve silenciosamente um registro existente.

## Stack técnica

| Camada | Tecnologias |
| --- | --- |
| Frontend | Next.js · React · TypeScript · CSS · (Vercel) |
| Backend | Java · Spring Boot (Web, Data JPA, Security) · Flyway · (Render) |
| Banco | PostgreSQL (Neon) |
| Importação | Apache POI (Excel) · Tess4J/Tesseract + OpenCV opcional (OCR) |
| Testes | JUnit 5 · Mockito · Spring Boot Test · Vitest · React Testing Library · Playwright |
| Acessibilidade | WCAG 2.2 · axe-core · Lighthouse |
| Infra | Git/GitHub · GitHub Actions (CI/CD) · Docker · OpenAPI/Swagger |

---

# Plano de Ação por Tasks

Marque `[x]` ao concluir. Divisão de responsáveis será definida mais adiante.

## Épico 0 — Fundação do projeto
- [ ] **T0.1** Inicializar repositório Git (monorepo `buscarm/` com `backend/` e `frontend/`)
- [ ] **T0.2** Proteger a branch `main` (merge só via PR aprovado) — ver [ADR-0004](./docs/adr/0004-estrategia-de-branches.md)
- [ ] **T0.3** Configurar `.gitignore`, licença, README, PR template e CODEOWNERS
- [ ] **T0.4** Criar contas/projetos: Neon (Postgres), Render (backend), Vercel (frontend)
- [ ] **T0.5** Definir variáveis de ambiente e `.env.example` (front e back)

## Épico 1 — Modelagem de dados
- [ ] **T1.1** Modelar `registro_matricula` (id, rm, nome_completo, sobrenome, nome, ano_opcional, situacao, criado_em, atualizado_em, ativo)
- [ ] **T1.2** Modelar `usuario` (id, nome, email, senha_hash, papel, ativo)
- [ ] **T1.3** Modelar `auditoria` (id, usuario_id, acao, entidade, entidade_id, dados_antes, dados_depois, criado_em)
- [ ] **T1.4** Modelar `importacao` (id, tipo[PLANILHA|IMAGEM], nome_arquivo, status, qtd_total, qtd_importada, qtd_ignorada, qtd_com_erro, criado_em, criado_por)
- [ ] **T1.5** Modelar `importacao_item` (id, importacao_id, linha_origem, rm_extraido, nome_extraido, status[PENDENTE|VALIDO|DUPLICADO|CONFLITO|INVALIDO|IMPORTADO|IGNORADO], mensagem_erro, registro_matricula_id)
- [ ] **T1.6** Escrever migrations Flyway (V1..Vn) + índices (RM único, índice de busca por nome)
- [ ] **T1.7** Estratégia de normalização de nome (case, acentos, `sobrenome, nome`) para busca

## Épico 2 — Backend base
- [ ] **T2.1** Bootstrap Spring Boot (Web, Data JPA, Security, Flyway, Validation, Actuator)
- [ ] **T2.2** Conexão com Neon (perfis: local/prod) + pool de conexões
- [ ] **T2.3** Entidades JPA + repositórios
- [ ] **T2.4** Camada DTO + mappers + tratamento global de erros (`@ControllerAdvice`)
- [ ] **T2.5** OpenAPI/Swagger habilitado
- [ ] **T2.6** Configurar CORS para o frontend

## Épico 3 — Autenticação e autorização
- [x] **T3.1** Spring Security + login com **JWT (stateless)** — `POST /api/auth/login` ([ADR-0002](./docs/adr/0002-autenticacao-jwt.md))
- [x] **T3.2** Papéis (`SECRETARIA`, `ADMIN`) e proteção de endpoints (filtro JWT + entry point 401)
- [x] **T3.3** Seed de usuário admin inicial (`UsuarioAdminSeeder`)
- [x] **T3.4** Hash de senha (BCrypt) e regras mínimas de segurança
- Frontend: tela `/login`, `AuthProvider`/`useAuth`, `RequireAuth` (rotas protegidas), token no `shared/api/client`

## Épico 4 — Cadastro manual e pesquisa
- [ ] **T4.1** API cadastro manual `POST /api/registros`
- [ ] **T4.2** API alteração `PUT /api/registros/{id}` (com auditoria)
- [ ] **T4.3** API desativação `PATCH` (soft delete, `ativo=false`)
- [ ] **T4.4** API busca por nome `GET /api/registros?nome=` (parcial, normalizada, acento-insensível)
- [ ] **T4.5** API busca por RM `GET /api/registros?rm=`
- [ ] **T4.6** Paginação e ordenação por sobrenome
- [ ] **T4.7** Tela de pesquisa (input nome/RM, resultados, destaque do RM)
- [ ] **T4.8** Tela/form de cadastro manual e edição
- [ ] **T4.9** Ação de desativar registro na UI

## Épico 5 — Importação por Excel *[coração do MVP]*
- [x] **T5.1** `LeitorPlanilhaPort` + adaptador Apache POI (.xlsx) — reconhece colunas Nome + RM
- [x] **T5.2** Normalização de registros (`RegistroExtraido` comum ao pipeline — ADR-0003)
- [x] **T5.3** `ValidadorRegistro`: VALIDO, INVALIDO (RM/nome ausente), DUPLICADO, CONFLITO (RM igual, nome diferente)
- [x] **T5.4** `POST /api/importacoes/planilha/validar` → retorna resumo + itens (NÃO grava)
- [x] **T5.5** `POST /api/importacoes/planilha/confirmar` → efetiva só os aprovados + relatório
- [x] **T5.6** `GET /api/importacoes/{id}` → status/relatório
- [x] **T5.7** Persistir `importacao` + `importacao_item` (rastreabilidade da origem) — Flyway V3
- [x] **T5.8** Tela de upload de planilha
- [x] **T5.9** Tela de pré-visualização (linha, RM, nome, situação; seleção dos válidos)
- [x] **T5.10** Tela de resultado/relatório da importação
- [x] **T5.11** Integração fim-a-fim front ↔ API
- [ ] **T5.12** Importar amostra controlada da planilha real (com a secretaria)

## Épico 6 — Exportação e backup
- [x] **T6.1** API exportação dos registros (.xlsx/.csv) — `GET /api/registros/exportar?formato=`
- [x] **T6.2** Estratégia de backup do banco (Neon PITR/branch + `scripts/backup.sh` pg_dump) — ver [docs/BACKUP.md](./docs/BACKUP.md)
- [x] **T6.3** Botão/tela de exportação na UI (feature `exportar-registros` em `/registros`)

## Épico 7 — Importação por fotografia / OCR (segunda etapa)
- [ ] **T7.1** `OCRService` com Tess4J/Tesseract (pt)
- [ ] **T7.2** Pré-processamento de imagem (OpenCV opcional: contraste, recorte, perspectiva)
- [ ] **T7.3** `ExtratorRegistroService`: linhas → (RM, nome) → `RegistroDTO` (reusa validador)
- [ ] **T7.4** `POST /api/importacoes/imagem/processar` e `.../confirmar`
- [ ] **T7.5** Tela de captura/upload de foto
- [ ] **T7.6** Tela de conferência/edição dos resultados do OCR (checkbox + correção inline)
- [ ] **T7.7** Integrar OCR ao mesmo pipeline da planilha
- [ ] **T7.8** Medir desempenho do reconhecimento (nomes/RMs corretos, correções, tempo)

## Épico 8 — Acessibilidade
- [ ] **T8.1** Semântica HTML5, foco, navegação por teclado, contraste (WCAG 2.2)
- [ ] **T8.2** Rótulos/aria em formulários e tabelas de pré-visualização
- [ ] **T8.3** Auditorias axe-core + Lighthouse e correções
- [ ] **T8.4** Estados acessíveis nas telas de importação

## Épico 9 — Auditoria e rastreabilidade
- [ ] **T9.1** Registrar toda ação sensível (criar/alterar/desativar/importar) na `auditoria`
- [ ] **T9.2** Endpoint/tela de consulta de auditoria (admin)
- [ ] **T9.3** Rastrear de onde cada registro importado veio (via `importacao_item`)

## Épico 10 — Testes
- [ ] **T10.1** Unit backend (JUnit 5 + Mockito): validador, leitor Excel, normalização
- [ ] **T10.2** Integração backend (Spring Boot Test): endpoints de importação e busca
- [ ] **T10.3** Unit frontend (Vitest + RTL): componentes de busca/preview
- [ ] **T10.4** E2E (Playwright): fluxo planilha → busca → RM
- [ ] **T10.5** Cobertura mínima acordada e relatório

## Épico 11 — DevOps / CI/CD / Deploy
- [ ] **T11.1** Dockerfile do backend
- [ ] **T11.2** GitHub Actions: build + testes (back e front) em PR — ver [ADR-0005](./docs/adr/0005-ci-cd.md)
- [ ] **T11.3** Versionamento (SemVer + tags) e geração de releases
- [ ] **T11.4** Deploy backend no Render (Docker) + variáveis
- [ ] **T11.5** Deploy frontend no Vercel + variáveis
- [ ] **T11.6** Banco Neon em produção + rodar migrations Flyway
- [ ] **T11.7** Smoke test em produção

## Épico 12 — Validação com a comunidade e entregas acadêmicas
- [ ] **T12.1** Apresentar 1ª versão à secretaria (feedback)
- [ ] **T12.2** Medir tempo: busca manual no livro × busca no BuscaRM
- [ ] **T12.3** Coletar métricas de importação (total, válidos, duplicados, conflitos, inválidos, tempo)
- [ ] **T12.4** Coletar métricas de OCR (reconhecimento, correções, tempo)
- [ ] **T12.5** Relatório Parcial
- [ ] **T12.6** Relatório Final + vídeo demonstrativo

---

## Mapeamento com as Quinzenas (planejamento.md)

- **Quinzena 3 — Projeto técnico:** Épicos 0, 1, 2, 3 + início do 4.
- **Quinzena 4 — Solução inicial:** Épico 5 (importação Excel) + Épico 4 + deploy inicial (Épico 11) + amostra da planilha + Relatório Parcial (T12.5).
- **Quinzena 5 — Imagem + melhorias:** Épico 7 (OCR) + Épico 6 + Épico 9 + Épico 8 + Épico 10.
- **Quinzena 6 — Avaliação:** Épico 12 (métricas: livro × Excel × foto+OCR).

## Fluxo mínimo demonstrável (para o vídeo)

```
Planilha → Importação → Busca pelo nome → RM → Localização do prontuário físico
```

## Próximos passos imediatos (ordem sugerida)
1. T0.1–T0.5 (fundação e contas de nuvem)
2. T1.1–T1.6 (modelagem + Flyway)
3. T2.1–T2.6 (backend base)
4. T4.1/T4.4/T4.5 + T4.7 (primeiro fluxo de busca ponta a ponta)
5. Épico 5 (importação Excel — o coração do MVP)
