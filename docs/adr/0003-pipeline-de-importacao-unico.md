# ADR-0003: Pipeline de importação único (Excel + OCR)

- **Status:** Aceito
- **Data:** 2026-08-07
- **Contexto de decisão:** Épicos 5 e 7 — importação

## Contexto

Há duas fontes de entrada em massa: planilha Excel (carga inicial, principal) e foto do livro
via OCR (segunda etapa). Ambas produzem os mesmos dados essenciais: **RM + Nome**. A base é
sensível — a última atualização do livro perdeu nomes, então não podemos sobrescrever
silenciosamente registros existentes.

## Decisão

Excel e OCR **convergem para um pipeline único**:

```
Fonte (Excel | Foto) → Extrator (POI | OCR) → RegistroDTO → ValidadorRegistroService
   → Pré-visualização → Confirmação humana → Persistência (registro + importacao_item)
```

- Um **`RegistroDTO`** comum é a saída de qualquer extrator (padrão *Strategy*).
- Um **único `ValidadorRegistroService`** classifica cada item: `VALIDO`, `INVALIDO`
  (RM/nome ausente), `DUPLICADO` (já cadastrado), `CONFLITO` (mesmo RM, nome diferente).
- Endpoints separados por fonte para **validar** (não grava) e **confirmar** (grava só aprovados).
- Toda importação é rastreada em `importacao` + `importacao_item` (de onde veio cada registro).

## Alternativas consideradas

- **Duas regras de negócio separadas (uma por fonte):** duplicaria validação/duplicidade,
  divergiria com o tempo e dobraria os testes. Descartado.
- **Upload → insert direto:** viola a regra de ouro (sem escrita silenciosa) e arrisca a base. Descartado.

## Consequências

- Positivas: reuso das regras, menos código, testes concentrados no validador; rastreabilidade total.
- Negativas: exige desenhar o `RegistroDTO` para servir às duas fontes desde o início.
