# Backup e recuperação

Três camadas de proteção dos dados, do mais automático ao mais manual.

## 1. Neon (gerenciado) — primeira linha
O PostgreSQL no Neon oferece **backup contínuo com point-in-time recovery (PITR)** e **branching**
do banco. Configuração recomendada:
- Manter o histórico de restauração no maior período disponível do plano.
- Antes de uma importação grande (Épico 5), criar um **branch** do banco no Neon como snapshot
  instantâneo — se algo der errado, basta apontar de volta.

## 2. Dump lógico periódico (`pg_dump`) — cópia externa
Um dump completo, versionável e independente do provedor. Use o script:

```
DATABASE_URL="postgres://usuario:senha@host/db?sslmode=require" ./scripts/backup.sh
```

Gera `backups/buscarm-AAAAMMDD-HHMMSS.dump` (formato custom, restaurável com `pg_restore`).
Recomenda-se rodar via **GitHub Actions agendado** (cron) ou na máquina da secretaria, guardando
as cópias fora do provedor (ex.: armazenamento da escola).

Restauração:
```
pg_restore --clean --if-exists --no-owner -d "$DATABASE_URL" backups/buscarm-XXXX.dump
```

## 3. Exportação pela aplicação — cópia de negócio
`GET /api/registros/exportar?formato=xlsx` (ou `csv`) baixa **todos os registros ativos**. É a
cópia que a secretaria consegue gerar sozinha, abrir no Excel e guardar — útil como backup
funcional do índice Nome→RM, além de conferência.

## Recomendações
- Testar a restauração periodicamente (backup não testado não é backup).
- Segredos (`DATABASE_URL`) nunca no repositório — ver `docs/CONFIGURACAO_NUVEM.md`.
- Antes de qualquer importação em lote: snapshot (Neon branch) **ou** `pg_dump`.
