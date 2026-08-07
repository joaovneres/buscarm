# ADR-0004: Estratégia de branches e proteção da `main`

- **Status:** Aceito
- **Data:** 2026-08-07
- **Contexto de decisão:** Épico 0 — fundação; T0.2

## Contexto

Equipe pequena, base de dados sensível e entregas acadêmicas por quinzena. Precisamos garantir
que nada entre na linha principal sem revisão, e manter um histórico limpo e rastreável.

## Decisão

Adotar **trunk-based leve com PR obrigatório**:

- **`main`** é a branch protegida e sempre "verde" (deployável). **Nunca** recebe push direto.
- Todo trabalho acontece em **branches curtas** a partir de `main`:
  `feat/<epico>-<descricao>`, `fix/...`, `chore/...`, `docs/...`.
- Integração **só via Pull Request** com **pelo menos 1 aprovação** e **CI verde**.
- Regras de proteção da `main` (GitHub ruleset/branch protection):
  - exigir PR antes de merge;
  - exigir ≥ 1 revisão aprovada;
  - exigir status checks (CI) aprovados;
  - exigir branch atualizada com a `main`;
  - bloquear force-push e exclusão da branch;
  - descartar aprovações obsoletas ao surgir novo commit.
- Commits seguem **Conventional Commits** (ver skill `git-workflow`).
- Merge preferencial: **squash** (histórico linear e legível).

## Alternativas consideradas

- **Git Flow completo (develop + release + hotfix):** cerimônia demais para o tamanho do time. Descartado.
- **Push direto na main:** inaceitável dada a sensibilidade dos dados e a exigência de revisão. Descartado.

## Consequências

- Positivas: revisão garantida, `main` estável, histórico limpo, base para CI/CD e deploy automático.
- Negativas: todo merge depende de um segundo aprovador (aceitável; em time de 2, cada um revisa o outro).
- Observação operacional: como owner/admin do repositório pode ter bypass, manter a regra de
  "não usar bypass" como acordo de equipe, ou aplicar a proteção também a administradores.
