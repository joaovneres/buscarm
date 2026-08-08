#!/usr/bin/env bash
# Backup lógico do banco do BuscaRM via pg_dump (formato custom, restaurável com pg_restore).
# Uso: DATABASE_URL="postgres://user:senha@host/db?sslmode=require" ./scripts/backup.sh
set -euo pipefail

if [ -z "${DATABASE_URL:-}" ]; then
  echo "Defina DATABASE_URL (ex.: postgres://user:senha@host/db?sslmode=require)" >&2
  exit 1
fi

DIR="${BACKUP_DIR:-backups}"
mkdir -p "$DIR"
ARQUIVO="$DIR/buscarm-$(date +%Y%m%d-%H%M%S).dump"

echo "Gerando backup em $ARQUIVO ..."
pg_dump --format=custom --no-owner --no-privileges --dbname="$DATABASE_URL" --file="$ARQUIVO"
echo "OK: $ARQUIVO"
echo "Restaurar com: pg_restore --clean --if-exists --no-owner -d \"\$DATABASE_URL\" $ARQUIVO"
