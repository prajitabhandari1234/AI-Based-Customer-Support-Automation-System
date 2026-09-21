#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:8080}"
ADMIN_EMAIL="${ADMIN_EMAIL:-admin@support.local}"
ADMIN_PASSWORD="${ADMIN_PASSWORD:-Admin123!}"
AGENT_EMAIL="${AGENT_EMAIL:-agent@support.local}"
AGENT_PASSWORD="${AGENT_PASSWORD:-Agent123!}"
CUSTOMER_EMAIL="${CUSTOMER_EMAIL:-customer@support.local}"
CUSTOMER_PASSWORD="${CUSTOMER_PASSWORD:-Customer123!}"
TMP_BODY="$(mktemp)"
trap 'rm -f "$TMP_BODY"' EXIT

PASS=0
FAIL=0
HTTP_CODE=""
BODY=""

request() {
  local method="$1" path="$2" token="${3:-}" body="${4:-}"
  local args=(-sS -o "$TMP_BODY" -w "%{http_code}" -X "$method" "${BASE_URL}${path}" -H "Accept: application/json")
  if [[ -n "$token" ]]; then
    args+=( -H "Authorization: Bearer $token" )
  fi
  if [[ -n "$body" ]]; then
    args+=( -H "Content-Type: application/json" --data "$body" )
  fi
  HTTP_CODE="$(curl "${args[@]}")"
  BODY="$(cat "$TMP_BODY")"
}

expect_status() {
  local expected="$1" label="$2"
  if [[ "$HTTP_CODE" == "$expected" ]]; then
    printf 'PASS  %-55s [%s]\n' "$label" "$HTTP_CODE"
    PASS=$((PASS + 1))
  else
    printf 'FAIL  %-55s expected=%s actual=%s\n' "$label" "$expected" "$HTTP_CODE"
    printf '      body: %s\n' "$BODY"
    FAIL=$((FAIL + 1))
  fi
}

json_get() {
  local path="$1"
  python3 - "$path" "$BODY" <<'PY'
import json, sys
path, raw = sys.argv[1], sys.argv[2]
try:
    value = json.loads(raw)
    for part in path.split('.'):
        if part == '':
            continue
        if isinstance(value, list):
            value = value[int(part)]
        else:
            value = value.get(part)
    if value is None:
        print('')
    elif isinstance(value, (dict, list)):
        print(json.dumps(value))
    else:
        print(value)
except Exception:
    print('')
PY
}

login() {
  local email="$1" password="$2"
  request POST /api/auth/login "" "{\"email\":\"$email\",\"password\":\"$password\"}"
  expect_status 200 "Login: $email"
  json_get token
}

echo "Running end-to-end API flow against: $BASE_URL"
echo "Expected startup mode: SEED_DATA=true, AI_PROVIDER=local, test H2 database."
echo

request GET /actuator/health
expect_status 200 "Public health endpoint"

ADMIN_TOKEN="$(login "$ADMIN_EMAIL" "$ADMIN_PASSWORD" | tail -n 1)"
AGENT_TOKEN="$(login "$AGENT_EMAIL" "$AGENT_PASSWORD" | tail -n 1)"
SEED_CUSTOMER_TOKEN="$(login "$CUSTOMER_EMAIL" "$CUSTOMER_PASSWORD" | tail -n 1)"

UNIQUE="$(date +%s)-$RANDOM"
NEW_EMAIL="e2e.${UNIQUE}@example.com"
request POST /api/auth/register "" "{\"name\":\"E2E Customer\",\"email\":\"$NEW_EMAIL\",\"password\":\"Password123!\"}"
expect_status 201 "Register a new customer"
CUSTOMER_TOKEN="$(json_get token)"

request GET /api/auth/me "$CUSTOMER_TOKEN"
expect_status 200 "Read current authenticated customer"

request GET /api/admin/users "$CUSTOMER_TOKEN"
expect_status 403 "Customer cannot access admin users"

request GET /api/agent/tickets "$CUSTOMER_TOKEN"
expect_status 403 "Customer cannot access agent tickets"

request POST /api/customer/conversations "$CUSTOMER_TOKEN"
expect_status 201 "Create customer conversation"
CONVERSATION_ID="$(json_get conversationId)"

request POST /api/customer/tickets "$CUSTOMER_TOKEN" "{\"conversationId\":$CONVERSATION_ID,\"title\":\"E2E existing conversation\",\"category\":\"ACCOUNT\",\"priority\":\"MEDIUM\"}"
expect_status 201 "Create ticket from existing conversation"
EXISTING_TICKET_ID="$(json_get ticketId)"

request GET "/api/customer/tickets/$EXISTING_TICKET_ID" "$CUSTOMER_TOKEN"
expect_status 200 "Read owned customer ticket details"

request POST /api/chat/messages "$CUSTOMER_TOKEN" '{"message":"write a python code to subtract two numbers"}'
expect_status 200 "Out-of-scope first chat is handled safely"
if [[ -n "$(json_get ticket.ticketId)" ]]; then
  echo "FAIL  Out-of-scope chat unexpectedly created a ticket"
  FAIL=$((FAIL + 1))
else
  echo "PASS  Out-of-scope chat created no ticket"
  PASS=$((PASS + 1))
fi

request POST /api/chat/messages "$CUSTOMER_TOKEN" "{\"message\":\"My app is not working and shows an error E2E-$UNIQUE\"}"
expect_status 200 "In-scope AI chat creates support ticket"
CHAT_TICKET_ID="$(json_get ticket.ticketId)"

request POST /api/tickets "$CUSTOMER_TOKEN" "{\"title\":\"E2E manual support ticket\",\"message\":\"My billing payment has an issue and I need human support\"}"
expect_status 201 "Customer creates manual escalated ticket"
MANUAL_TICKET_ID="$(json_get ticketId)"

request GET /api/tickets/my "$CUSTOMER_TOKEN"
expect_status 200 "Customer lists own tickets"

request GET /api/tickets/my/summary "$CUSTOMER_TOKEN"
expect_status 200 "Customer reads ticket summary counts"

request GET /api/agent/tickets "$AGENT_TOKEN"
expect_status 200 "Agent lists available/assigned escalated tickets"

request PUT "/api/agent/tickets/$MANUAL_TICKET_ID/assign" "$AGENT_TOKEN"
expect_status 200 "Agent assigns manual ticket to self"

request POST "/api/agent/tickets/$MANUAL_TICKET_ID/messages" "$AGENT_TOKEN" '{"content":"E2E agent response: I am reviewing your billing issue."}'
expect_status 200 "Assigned agent sends ticket response"

request PATCH "/api/agent/tickets/$MANUAL_TICKET_ID/status" "$AGENT_TOKEN" '{"status":"RESOLVED","resolutionNotes":"Resolved by E2E workflow"}'
expect_status 200 "Agent resolves ticket"

request GET /api/customer/notifications "$CUSTOMER_TOKEN"
expect_status 200 "Customer lists generated notifications"
NOTIFICATION_ID="$(json_get 0.notificationId)"
if [[ -n "$NOTIFICATION_ID" ]]; then
  request PUT "/api/customer/notifications/$NOTIFICATION_ID/read" "$CUSTOMER_TOKEN"
  expect_status 200 "Customer marks own notification as read"
fi

request POST /api/knowledge-base "$ADMIN_TOKEN" "{\"questionPattern\":\"e2e unique help $UNIQUE\",\"answerTemplate\":\"E2E test answer\",\"category\":\"GENERAL_INQUIRY\",\"active\":true}"
expect_status 201 "Admin creates knowledge-base entry"
KB_ID="$(json_get kbId)"

request PUT "/api/knowledge-base/$KB_ID" "$ADMIN_TOKEN" "{\"questionPattern\":\"e2e unique help updated $UNIQUE\",\"answerTemplate\":\"Updated E2E answer\",\"category\":\"GENERAL_INQUIRY\",\"active\":false}"
expect_status 200 "Admin updates knowledge-base entry"

request GET /api/analytics/summary "$ADMIN_TOKEN"
expect_status 200 "Admin reads analytics summary"

TODAY="$(date +%F)"
request GET "/api/admin/analytics/reports/weekly?date=$TODAY" "$ADMIN_TOKEN"
expect_status 200 "Admin generates weekly analytics report"

request GET /api/system-logs "$ADMIN_TOKEN"
expect_status 200 "Admin reads system logs"

request DELETE "/api/knowledge-base/$KB_ID" "$ADMIN_TOKEN"
expect_status 204 "Admin deletes E2E knowledge-base entry"

request POST /api/auth/login "" '{"email":"customer@support.local","password":"wrong"}'
expect_status 401 "Wrong password is rejected"

request POST /api/auth/register "" '{"name":"x","email":"bad","password":"short"}'
expect_status 400 "Registration validation rejects malformed payload"

request POST /api/auth/login "" '{"email":"not-an-email","password":"x"}'
expect_status 400 "Login validation rejects invalid email"

echo
echo "E2E result: $PASS passed, $FAIL failed"
if [[ "$FAIL" -gt 0 ]]; then
  exit 1
fi
