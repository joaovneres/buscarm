# Repositório e proteção da branch `main`

Repositório: **https://github.com/joaovneres/buscarm** (privado).
Decisão de branches/proteção: [ADR-0004](./adr/0004-estrategia-de-branches.md).

## Estado atual (já configurado)
- Repositório criado no GitHub e `main` publicada.
- **Ruleset `protecao-main` ativo** (id 20569918) em modo *bootstrap*:
  - PR obrigatório para mesclar na `main`;
  - **≥ 1 aprovação**;
  - resolução de threads exigida; descarte de aprovações obsoletas ao novo push;
  - bloqueio de force-push e de exclusão da branch;
  - **bypass**: papel *admin* do repositório (owner) pode mesclar sozinho durante o bootstrap.
- `.github/`: PR template, CODEOWNERS, ruleset versionado (`main-protection.json`).

## Duas fases da proteção (importante)
1. **Agora (bootstrap):** a regra **não** exige os status checks de CI, porque ainda não há
   código para o CI compilar — exigi-los travaria os PRs de scaffold.
2. **Depois do 1º CI verde** (após o scaffold do backend/Épico 2 e frontend/Épico 4): apertar a
   regra para exigir os checks `backend`, `frontend` e `e2e`. O `main-protection.json` versionado
   já representa esse estado final; para aplicá-lo:

```
gh api -X PUT repos/joaovneres/buscarm/rulesets/20569918 \
  --input .github/rulesets/main-protection.json
```

## Fluxo diário
```
git switch -c feat/e2-backend-base main
# ... trabalho + commits (Conventional Commits — skill git-workflow) ...
git push -u origin feat/e2-backend-base
gh pr create --fill        # abre o PR usando o template
```
- Merge normal: precisa de 1 aprovação + (futuramente) CI verde.
- Durante o bootstrap solo, você pode mesclar com bypass de admin:
  `gh pr merge <n> --squash --admin`.

## Quando o Aluno 2 entrar
```
gh api -X PUT repos/joaovneres/buscarm/collaborators/<usuario> -f permission=push
```
Com um segundo revisor, os PRs passam a ser aprovados de verdade e o bypass de admin deixa de
ser necessário no dia a dia (pode ser removido do ruleset se quiserem endurecer).

## Segredos de deploy (mais tarde — ver CONFIGURACAO_NUVEM.md)
```
gh secret set RENDER_DEPLOY_HOOK_URL --repo joaovneres/buscarm
gh secret set VERCEL_DEPLOY_HOOK_URL --repo joaovneres/buscarm
```
