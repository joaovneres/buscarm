# ADR-0001: Arquitetura e stack

- **Status:** Aceito
- **Data:** 2026-08-07
- **Contexto de decisão:** Épico 0/1/2 — fundação do projeto

## Contexto

Precisamos informatizar o índice Nome → RM de uma escola (+6.000 registros), com carga inicial
por planilha Excel e, futuramente, por foto do livro (OCR). A solução precisa ir para nuvem, ter
testes, acessibilidade e ser mantível por uma equipe pequena de estudantes. As tecnologias já
foram levantadas em `tec.md`.

## Decisão

Adotar arquitetura **cliente/servidor desacoplada**:

- **Frontend:** Next.js + React + TypeScript, hospedado na **Vercel**.
- **Backend:** Java + Spring Boot (Web, Data JPA, Security) + Flyway, em container **Docker** no **Render**.
- **Banco:** PostgreSQL gerenciado no **Neon**.
- Comunicação **REST/JSON** sobre HTTPS; contrato documentado com **OpenAPI/Swagger**.
- Backend em **camadas** (Controller → Service → Repository) e organização **package-by-feature**.
- Frontend **feature-first** com App Router (detalhes em `docs/arquitetura.md`).

## Alternativas consideradas

- **Monólito full-stack (só Next.js + rotas de API):** menos partes, mas o time domina Java/Spring
  e o PI valoriza back e front separados; OCR/Apache POI são mais naturais em Java. Descartado.
- **Backend em Node/Express:** perderia o ecossistema Spring (Security, Data JPA, Flyway, POI, Tess4J). Descartado.

## Consequências

- Positivas: separação clara, cada camada testável e deployável; alinhado a `tec.md` e ao currículo.
- Negativas: dois deploys e CORS para gerenciar; contrato front/back precisa ser mantido em sincronia (mitigado por OpenAPI + tipos TS).
