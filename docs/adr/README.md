# Architecture Decision Records (ADRs)

Registramos aqui as **decisões arquiteturais** do BuscaRM — o que decidimos, por quê, e quais
alternativas foram descartadas. Cada decisão relevante vira um arquivo numerado e imutável;
quando mudamos de ideia, criamos um novo ADR que **substitui** (supersedes) o anterior em vez
de reescrever a história.

## Como usar

1. Copie [`0000-template.md`](./0000-template.md) para `NNNN-titulo-curto.md` (número sequencial).
2. Preencha contexto, decisão, consequências.
3. Abra em PR — a decisão é revisada junto com quem participa do projeto.
4. Ao mudar uma decisão, crie um novo ADR e marque o antigo como `Substituído por ADR-XXXX`.

> Para escrever um ADR seguindo o padrão, use a skill `adr` em `.claude/skills/adr/`.

## Índice

| ADR | Título | Status |
| --- | --- | --- |
| [0001](./0001-arquitetura-e-stack.md) | Arquitetura e stack | Aceito |
| [0002](./0002-autenticacao-jwt.md) | Autenticação com JWT stateless | Aceito |
| [0003](./0003-pipeline-de-importacao-unico.md) | Pipeline de importação único (Excel + OCR) | Aceito |
| [0004](./0004-estrategia-de-branches.md) | Estratégia de branches e proteção da `main` | Aceito |
| [0005](./0005-ci-cd.md) | CI/CD, versionamento e deploy | Aceito |
| [0006](./0006-auditoria-de-autoria.md) | Auditoria de autoria (criado_por/atualizado_por + trigger) | Aceito |
| [0007](./0007-backend-hexagonal.md) | Arquitetura Hexagonal no backend (Ports & Adapters) | Aceito |
