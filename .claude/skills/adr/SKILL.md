---
name: adr
description: Como registrar decisões de arquitetura do BuscaRM como ADRs. Use quando uma escolha técnica relevante for feita ou mudada (framework, padrão, estratégia de auth, deploy, modelagem estrutural). Padroniza a criação de arquivos em docs/adr/.
---

# ADR — Architecture Decision Records (BuscaRM)

Registra decisões arquiteturais em `docs/adr/` de forma imutável e rastreável.
Índice e regras gerais: [`docs/adr/README.md`](../../../docs/adr/README.md).

## Quando criar um ADR

Crie quando a decisão for **estrutural e cara de reverter**, por exemplo:
- escolha/troca de framework, biblioteca ou serviço (ex.: Maven vs Gradle, Neon vs outro Postgres);
- estratégia transversal (auth, versionamento, deploy, estrutura de pastas, pipeline de importação);
- padrão que o time deve seguir dali em diante.

**Não** crie ADR para detalhe de implementação trivial ou correção pontual.

## Como criar

1. Copie `docs/adr/0000-template.md` para `docs/adr/NNNN-titulo-curto.md`
   (`NNNN` = próximo número sequencial, com zeros à esquerda).
2. Preencha: **Status** (Proposto→Aceito), **Data** (AAAA-MM-DD), **Contexto de decisão** (épico/task).
3. Escreva as seções: **Contexto** (forças/restrições), **Decisão** (afirmativa e clara),
   **Alternativas consideradas** (com motivo de descarte), **Consequências** (positivas e trade-offs).
4. Adicione a linha no índice de `docs/adr/README.md`.
5. Abra em PR (skill `git-workflow`) — a decisão é revisada junto.

## Regras

- ADR é **imutável** depois de Aceito. Mudou de ideia? Crie um **novo** ADR e marque o antigo como
  `Substituído por ADR-XXXX` (atualize só o campo Status e o índice).
- Um ADR = uma decisão. Título curto no imperativo/substantivo (ex.: "Autenticação com JWT").
- Seja honesto nas alternativas e nos trade-offs — o valor do ADR é explicar o **porquê** para o
  futuro (inclusive para o relatório acadêmico do PI).
- Linguagem em português, objetiva.

## Decisões já registradas

0001 Arquitetura/stack · 0002 Auth JWT · 0003 Pipeline de importação único ·
0004 Branches/proteção da main · 0005 CI/CD (proposto).
