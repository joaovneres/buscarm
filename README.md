# BuscaRM

Sistema web para tornar pesquisável o índice **Nome → RM** do livro de Registro de Matrícula
de uma escola, permitindo à secretaria localizar rapidamente o número do RM e, com ele, o
prontuário físico do aluno.

> Não digitalizamos o prontuário — digitalizamos o **índice** necessário para encontrá-lo.

## Estrutura do repositório

```
buscarm/
├── backend/          # API Spring Boot (Java) — Web, Data JPA, Security, Flyway
├── frontend/         # Aplicação Next.js (React + TypeScript)
├── docs/             # Documentação técnica e acadêmica
├── PLANO_DE_ACAO.md  # Todo list / plano de ação dividido em tasks e quinzenas
└── README.md
```

## Funcionalidades (MVP)

- Autenticação da secretaria
- Cadastro manual, alteração e desativação de registros
- **Pesquisa por nome** e por RM
- **Importação por planilha Excel** (Apache POI) com pré-visualização, validação de
  duplicidades/conflitos e confirmação humana
- Exportação e backup dos registros
- Auditoria e rastreabilidade das importações
- Acessibilidade (WCAG 2.2)

### Segunda etapa
- Importação assistida por **fotografia** do livro (OCR/Tesseract)

## Stack

Next.js · React · TypeScript · Java · Spring Boot · PostgreSQL (Neon) · Apache POI ·
Tesseract/Tess4J · Vercel · Render · GitHub Actions · Docker.

## Como começar

Veja o [PLANO_DE_ACAO.md](./PLANO_DE_ACAO.md) para o passo a passo dividido em tasks.

Instruções de execução do backend e frontend serão adicionadas em `backend/README.md` e
`frontend/README.md` conforme a implementação avança (Épicos 2 e 4).

## Deploy

- **Runbook passo a passo:** [`docs/deploy.md`](./docs/deploy.md) — Neon → Render → Vercel,
  com deploy manual versionado (ADR-0005).
- **Segredos e onde moram:** [`docs/CONFIGURACAO_NUVEM.md`](./docs/CONFIGURACAO_NUVEM.md).
- **Blueprint do backend:** [`render.yaml`](./render.yaml).
