---
name: git-workflow
description: Padrão de branches, commits (Conventional Commits) e Pull Requests do BuscaRM. Use ao criar branches, escrever mensagens de commit, abrir PRs ou preparar merges. Garante histórico limpo e a regra de que a main só entra via PR aprovado.
---

# Git Workflow — BuscaRM

Padroniza como criamos branches, commits e PRs. Baseado no [ADR-0004](../../../docs/adr/0004-estrategia-de-branches.md).

## Regra inviolável

- **`main` nunca recebe push direto.** Só entra via **Pull Request** com **≥ 1 aprovação** e **CI verde**.
- `main` está sempre deployável ("verde").

## Branches

Sempre a partir da `main` atualizada. Nomeie por tipo + escopo curto:

```
feat/<epico>-<descricao-curta>     ex.: feat/e5-importacao-planilha
fix/<descricao>                    ex.: fix/busca-acento-insensivel
chore/<descricao>                  ex.: chore/config-flyway
docs/<descricao>                   ex.: docs/adr-ci-cd
test/<descricao>                   refactor/<descricao>
```

Branches são **curtas** (dias, não semanas). Rebase na `main` antes de abrir/atualizar o PR.

## Commits — Conventional Commits

Formato: `tipo(escopo opcional): resumo no imperativo`

```
feat(importacao): valida duplicidade de RM na pré-visualização
fix(busca): normaliza acentos antes do LIKE
docs(adr): adiciona ADR-0005 de CI/CD
test(registro): cobre soft delete no service
chore(ci): adiciona workflow de build do backend
refactor(validador): extrai regra de conflito para método próprio
```

Tipos: `feat`, `fix`, `docs`, `test`, `refactor`, `chore`, `build`, `ci`, `perf`, `style`.

Regras:
- Resumo em **português, imperativo, minúsculo**, sem ponto final, ≤ ~72 caracteres.
- Um commit = uma mudança lógica coesa. Nada de "wip" ou "ajustes" na `main`.
- Corpo (opcional) explica o **porquê**; rodapé referencia a task: `Refs: T5.4`.
- **Não** commitar segredos, `.env`, binários grandes, `target/`, `node_modules/`.

## Pull Requests

- Título no mesmo estilo do commit principal.
- Descrição usando o template (`.github/pull_request_template.md`): o que muda, por quê, task
  relacionada (`Refs: TX.Y`), como testar, checklist (testes, acessibilidade se front, docs/ADR se decisão).
- PR pequeno e focado (idealmente < ~400 linhas de diff).
- **Merge por squash** para manter histórico linear.
- Só faça merge com CI verde e aprovação. Não use bypass de admin.

## Antes de abrir o PR (checklist rápido)

1. Testes locais passam (back: `mvn test`/`gradle test`; front: `npm test`).
2. Lint/format OK.
3. Branch rebaseada na `main`.
4. Sem arquivos indevidos no diff (`git status`, `git diff --staged`).
5. Se a mudança é uma decisão arquitetural → criar/atualizar um ADR (skill `adr`).
