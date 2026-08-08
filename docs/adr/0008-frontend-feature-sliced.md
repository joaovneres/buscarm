# ADR-0008: Arquitetura modular no frontend (Feature-Sliced) — não Microfrontend

- **Status:** Aceito
- **Data:** 2026-08-07
- **Contexto de decisão:** revisão de arquitetura do frontend — refina [ADR-0001](./0001-arquitetura-e-stack.md)

## Contexto

Precisávamos escolher e justificar um padrão de arquitetura para o frontend Next.js. A questão
levantada foi: **Microfrontend ou outra abordagem?** O BuscaRM é uma aplicação web única, mantida
por um time pequeno (2 pessoas), com um único deploy na Vercel e um domínio coeso (buscar/cadastrar
registros, importar por Excel e por foto).

## Decisão

Adotar uma **arquitetura modular por camadas orientada a features (Feature-Sliced Design)**, e
**não** Microfrontend. Camadas, de cima para baixo (a de cima só importa das de baixo):

```
app        # rotas Next (App Router) — composição das telas
features/  # casos de uso interativos (ex.: busca-registros) — ui/ + model/ (hooks)
entities/  # modelos de domínio do front (ex.: registro) — model/ (types) + api/ (acesso a dados)
shared/    # infra reutilizável: api/client, ui, config
```

Regras:
- **Direção de dependência única:** `app → features → entities → shared`. Uma feature não importa
  outra feature; entities não conhecem features.
- **Client vs Server Components:** server por padrão; `"use client"` só onde há estado/interação.
- **Acesso a dados** da entidade em `entities/<x>/api`, sobre o `shared/api/client` (único ponto de fetch).
- **Estado/lógica** de um caso de uso em `features/<x>/model` (hooks); UI em `features/<x>/ui`.

## Alternativas consideradas

- **Microfrontend** (ex.: Module Federation, múltiplos apps compostos): resolve escala
  **organizacional** — vários times entregando e implantando partes de forma independente. Aqui
  **não há esse problema**: um time, um repositório, um deploy. Traria custo de build, orquestração,
  versionamento entre remotes e duplicação de dependências (React em cada remote), sem benefício.
  **Descartado.**
- **Estrutura por tipo** (`components/`, `hooks/`, `services/` globais): simples no começo, mas
  espalha o mesmo caso de uso por várias pastas e não escala em coesão. Descartado.
- **Feature-first simples** (só `features/`): bom, mas sem a distinção `entities/shared` a
  reutilização e a direção de dependência ficam menos claras. Evoluímos para Feature-Sliced.

## Consequências

- Positivas: alta coesão por domínio, limites de dependência explícitos, fácil de testar
  (`useBuscaRegistros` e componentes isolados); a importação (Excel/foto) entra como novas features
  reusando `entities/registro` e `shared/api`.
- Negativas/trade-offs: mais pastas/indireção que uma estrutura plana; exige disciplina para
  respeitar a direção das camadas (reforçada na skill `frontend-dev`).
- Se um dia o produto crescer para múltiplos times/domínios independentes, reavaliar Microfrontend
  em um novo ADR.
