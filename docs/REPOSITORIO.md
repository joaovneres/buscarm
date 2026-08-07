# Repositório e proteção da branch `main`

Este guia cria o repositório no GitHub e ativa a regra de que a **`main` só é mergeada via PR
aprovado** (ADR-0004). O repositório **local** já está inicializado com os arquivos de
configuração (`.gitignore`, PR template, CODEOWNERS, ruleset). Falta apenas publicar no GitHub
e ligar a proteção — passos que exigem sua autenticação.

## Estado atual (feito automaticamente)
- `git init` na pasta `buscarm/`, branch principal `main`, primeiro commit.
- `.github/pull_request_template.md`, `.github/CODEOWNERS`, `.github/rulesets/main-protection.json`.

## 1. Pré-requisito: GitHub CLI (recomendado)

O `gh` não está instalado nesta máquina. Instale e autentique (rode no seu terminal — no Claude
Code, prefixe com `!`):

```
winget install --id GitHub.cli -e
gh auth login
```

> Sem o `gh`, dá para criar o repo pela web (github.com → New repository) e depois configurar a
> proteção pela interface (Settings → Rules → Rulesets), replicando o `main-protection.json`.

## 2. Criar o repositório remoto e publicar

Com o `gh` autenticado, dentro de `buscarm/`:

```
gh repo create buscarm --private --source=. --remote=origin --push
```

(Use `--public` se preferir. `--source=.` sobe o commit já existente.)

## 3. Ativar a proteção da `main`

### Opção A — via ruleset (recomendado, usa o arquivo versionado)

```
gh api -X POST repos/{owner}/buscarm/rulesets \
  --input .github/rulesets/main-protection.json
```

Isso aplica: PR obrigatório, ≥ 1 aprovação, status checks `backend` e `frontend` obrigatórios,
branch atualizada, resolução de threads, e bloqueio de force-push/exclusão da `main`.

### Opção B — branch protection clássica

```
gh api -X PUT repos/{owner}/buscarm/branches/main/protection \
  -H "Accept: application/vnd.github+json" \
  -f "required_pull_request_reviews[required_approving_review_count]=1" \
  -F "required_pull_request_reviews[dismiss_stale_reviews]=true" \
  -F "enforce_admins=true" \
  -F "required_status_checks[strict]=true" \
  -f "required_status_checks[contexts][]=backend" \
  -f "required_status_checks[contexts][]=frontend" \
  -F "restrictions=null" \
  -F "allow_force_pushes=false" \
  -F "allow_deletions=false"
```

## 4. Ajustes finais
- Edite `.github/CODEOWNERS` trocando `@OWNER` pelos handles reais (habilite "require code owner
  review" no ruleset se quiser torná-lo obrigatório).
- Os checks `backend` e `frontend` passam a existir quando os workflows do CI forem criados
  (Épico 11 / ADR-0005). Até lá, deixe-os como "não obrigatórios" ou ajuste os nomes.

## 5. Fluxo diário (a partir daqui)
```
git switch -c feat/e2-backend-base main
# ... trabalho + commits (Conventional Commits, skill git-workflow) ...
git push -u origin feat/e2-backend-base
gh pr create --fill        # abre o PR usando o template
# merge só após aprovação + CI verde (squash)
```
