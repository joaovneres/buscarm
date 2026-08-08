# ADR-0006: Auditoria de autoria (quem criou / quem atualizou)

- **Status:** Aceito
- **Data:** 2026-08-07
- **Contexto de decisão:** Épico 2/9 — auditoria dos registros

## Contexto

Cada registro de matrícula precisa saber **quem o criou** e **quem o atualizou por último**, e essa
autoria deve refletir o **usuário da sessão** que fez a operação. Como a base é sensível (a última
atualização do livro perdeu nomes), queremos garantia de integridade também **no banco**, não só
na aplicação.

## Decisão

Auditoria de autoria em **duas camadas**:

1. **Aplicação (Spring Data JPA Auditing):** a entidade usa `@CreatedBy`/`@LastModifiedBy`
   (colunas `criado_por`/`atualizado_por`) e `@CreatedDate`/`@LastModifiedDate`. Um
   `AuditorAware<String>` fornece o usuário atual via `UsuarioAtualProvider` (hoje "sistema";
   passa a ser o usuário do JWT no Épico 3 — ADR-0002).

2. **Banco (Postgres, fonte de verdade):** antes de cada transação de escrita, a aplicação
   define a GUC `app.current_username` na sessão do banco, local à transação
   (`set_config(..., true)`, em `PostgresUsuarioSessaoBanco`). Um **trigger** `BEFORE INSERT/UPDATE`
   (migration V2) **grava e valida** `criado_por`/`atualizado_por` a partir dessa GUC e **bloqueia**
   qualquer gravação sem usuário de sessão definido. No UPDATE, `criado_por` é preservado.

Nos testes (H2) não há GUC nem trigger; usa-se `NoOpUsuarioSessaoBanco` e a autoria é exercida
apenas pelo JPA Auditing.

## Alternativas consideradas

- **Só auditoria na aplicação:** simples, mas confia inteiramente no código; uma escrita fora do
  fluxo (script, outra app) não teria autoria garantida. Descartado como única camada.
- **Só trigger, sem JPA Auditing:** perderia a portabilidade dos testes (H2) e a integração com o
  contexto de segurança da aplicação. Descartado.
- **Coluna preenchida manualmente no service:** propenso a esquecimento; o trigger garante mesmo
  em caminhos futuros (importação em lote). Descartado.

## Consequências

- Positivas: autoria confiável e validada no banco; alinhada ao usuário da sessão; pronta para o
  JWT do Épico 3; o trigger cobre também a futura importação em lote (que deverá definir a GUC).
- Negativas/trade-offs: todo caminho de escrita precisa definir `app.current_username` (senão o
  trigger bloqueia — por design); o trigger é específico do Postgres e não é exercido nos testes
  em H2 (validado em ambiente Postgres).
