#!/usr/bin/env bash
set -euo pipefail

export APP_DB_MODE=h2
export H2_DB_URL="${H2_DB_URL:-jdbc:h2:file:./data/e2e-supportdb;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE}"
export JPA_DDL_AUTO="${JPA_DDL_AUTO:-create-drop}"
export SEED_DATA=true
export AI_PROVIDER=local
export OPENAI_API_KEY=""
export SERVER_PORT="${SERVER_PORT:-8080}"
export H2_CONSOLE_ENABLED=false

exec ./mvnw spring-boot:run
