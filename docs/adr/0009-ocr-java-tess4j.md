# ADR-0009: OCR por foto em Java (Tess4J), como adaptador de porta

- **Status:** Aceito
- **Data:** 2026-08-08
- **Contexto de decisão:** Épico 7 — importação por fotografia

## Contexto

A importação por foto (2ª etapa do MVP) precisa extrair `RM + Nome` de páginas do livro. A
dúvida foi: fazer o OCR **no próprio backend Java** (Tess4J/Tesseract) ou criar um **micro
serviço Python** (OpenCV + Tesseract/EasyOCR)?

Fato-chave da arquitetura: no hexagonal, o OCR é apenas uma **porta de saída**
`ExtratorImagemPort.extrair(bytes) → List<RegistroExtraido>`, cujo resultado cai no **mesmo**
`ValidadorRegistro`/pipeline/persistência já usados pelo Excel (ADR-0003). Ou seja, *onde* o OCR
roda é um detalhe de infraestrutura atrás de uma interface — a escolha é plugável e reversível.

## Decisão

Implementar o OCR **em Java com Tess4J** (`Tess4jExtratorAdapter`), dentro do backend existente:

- Um serviço só, um deploy, uma linguagem — menor custo operacional para um time de 2 pessoas.
- O adaptador fica em `@Profile("!test")`; o Tesseract nativo (pt) é instalado no Dockerfile.
- O texto do OCR passa por um parser de domínio puro `ExtratorLinhasOcr` (RM no início ou no fim
  da linha), reutilizado por um **stub de teste** (`@Profile("test")`) — assim o pipeline é
  testado no CI sem depender do binário nativo.
- Fluxo: `POST /imagem/processar` (OCR → conferência, **não grava**) e `POST /imagem/confirmar`
  (grava os itens **revisados/corrigidos** pelo funcionário).

## Alternativas consideradas

- **Micro serviço Python:** ecossistema de OCR/visão melhor (pré-processamento, EasyOCR/PaddleOCR
  para manuscrito), deps pesadas isoladas. Porém +1 serviço/deploy/CI e 2ª stack — custo alto para
  o MVP. **Descartado por ora** (mas fácil de adotar: bastaria um adaptador HTTP da mesma porta).
- **API de visão em nuvem:** melhor acurácia (inclui manuscrito), sem infra de OCR; porém custo,
  dependência externa e privacidade dos dados dos alunos. Descartado por ora.

## Consequências

- Positivas: complexidade mínima; reuso total do validador/pipeline; testável no CI via stub.
- Negativas/trade-offs: Tesseract é fraco em **manuscrito** — se as páginas forem manuscritas,
  trocamos o adaptador por Python/HTR (sem tocar domínio/regras). A imagem Docker fica maior
  (Tesseract nativo). Ajuste fino de qualidade fica para a avaliação de campo (T7.8).
