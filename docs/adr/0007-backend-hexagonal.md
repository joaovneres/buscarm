# ADR-0007: Arquitetura Hexagonal no backend (Ports & Adapters)

- **Status:** Aceito
- **Data:** 2026-08-07
- **Contexto de decisão:** revisão de arquitetura do backend — refina [ADR-0001](./0001-arquitetura-e-stack.md)

## Contexto

O ADR-0001 definiu o backend "em camadas" (Controller → Service → Repository). Ao evoluir o
projeto, o coração do BuscaRM se mostrou ser um conjunto de **regras de domínio** — normalização
de nome, unicidade de RM, e (adiante) validação de duplicidade/conflito da importação — que
precisa ser alimentado por **múltiplas fontes de entrada** (cadastro manual via REST, planilha
Excel via Apache POI, foto via OCR — ver ADR-0003) e persistido por **adaptadores de saída**
(PostgreSQL hoje, exportação/backup depois). Na arquitetura em camadas tradicional, a regra de
negócio tende a ficar acoplada a Spring/JPA, dificultando reúso entre as fontes e os testes.

## Decisão

Adotar **Arquitetura Hexagonal (Ports & Adapters)**. Por feature (começando por `registro`):

```
registro
├── domain           # modelo puro + regras (sem framework): Registro, NomeNormalizer, exceptions
├── application      # casos de uso e portas
│   ├── port/in      # portas de entrada (CadastrarRegistroUseCase, BuscarRegistrosUseCase)
│   ├── port/out     # portas de saída (RegistroRepositoryPort, UsuarioAtualPort, UsuarioSessaoPort)
│   └── RegistroApplicationService   # implementa as portas de entrada, orquestra as de saída
└── adapter
    ├── in/web        # RestController + DTOs (adaptador condutor)
    └── out/persistence  # JPA entity + Spring Data + adaptador que implementa a porta
```

Regras:
- **Domínio e aplicação não dependem de Spring/JPA** — só de interfaces (portas). Dependências
  apontam para dentro (regra da dependência).
- **Adaptadores** (web, persistência, segurança) implementam/consomem as portas e ficam na borda.
- Cross-cutting em `shared` (ApiError, Pagina) e `config`.
- A camada de segurança fornece adaptadores para `UsuarioAtualPort` (usuário da sessão) e
  `UsuarioSessaoPort` (propagação para a GUC do banco — ADR-0006).

## Alternativas consideradas

- **MVC em camadas (atual):** simples e familiar, mas acopla regra a framework e duplicaria a
  lógica entre Excel e OCR; testes exigem contexto Spring. Descartado como alvo.
- **DDD tático completo (agregados, repositórios ricos, eventos):** poderoso, porém cerimônia
  excessiva para um MVP de time pequeno; adotamos o essencial (modelo de domínio isolado) sem o
  peso completo. Descartado por ora.
- **Hexagonal (escolhido):** isola o domínio, torna o pipeline de importação um caso natural de
  múltiplos adaptadores de entrada convergindo numa porta, e permite testar a aplicação com mocks.

## Consequências

- Positivas: regras testáveis sem Spring (novo `RegistroApplicationServiceTest` roda com mocks);
  Excel/OCR (Épicos 5 e 7) entram como adaptadores para a mesma porta, sem reescrever regra;
  troca de infra (banco, framework) fica localizada nos adaptadores.
- Negativas/trade-offs: mais arquivos e indireção (portas + mappers domínio↔entidade); exige
  disciplina para não vazar tipos de infra (JPA/Spring) para dentro. Documentado nas skills.
