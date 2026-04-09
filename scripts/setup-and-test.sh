#!/bin/bash
# =============================================================================
# Niyamitra Platform - Initial Data Setup & API Test Script
# =============================================================================
# Usage:
#   chmod +x scripts/setup-and-test.sh
#   ./scripts/setup-and-test.sh
#
# Prerequisites:
#   - Docker Compose infrastructure running (docker-compose up -d)
#   - All Spring Boot services running on their respective ports
#     - niyamitra-profile-service   : 8081
#     - anupalan-rule-service       : 8082
#     - niyamitra-document-vault    : 8083
#     - niyamitra-task-service      : 8084
#     - kavach-notification-service : 8085
#     - kavach-floor-manager        : 8086
#     - niyamitra-api-gateway       : 8090
# =============================================================================

set -e

BASE_URL="http://localhost"
PROFILE_URL="$BASE_URL:8081"
ANUPALAN_URL="$BASE_URL:8082"
DOCUMENT_URL="$BASE_URL:8083"
TASK_URL="$BASE_URL:8084"
NOTIFICATION_URL="$BASE_URL:8085"
KAVACH_URL="$BASE_URL:8086"
GATEWAY_URL="$BASE_URL:8090"

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_header() {
    echo ""
    echo -e "${BLUE}=============================================================================${NC}"
    echo -e "${BLUE}  $1${NC}"
    echo -e "${BLUE}=============================================================================${NC}"
}

print_step() {
    echo ""
    echo -e "${YELLOW}>>> $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

check_service() {
    local name=$1
    local url=$2
    if curl -s -o /dev/null -w "%{http_code}" "$url" | grep -q "404\|200\|405"; then
        print_success "$name is running"
        return 0
    else
        print_error "$name is NOT running at $url"
        return 1
    fi
}

# =============================================================================
# STEP 0: Health Check - Verify all services are running
# =============================================================================
print_header "STEP 0: Health Check - Verifying Services"

check_service "Profile Service (8081)" "$PROFILE_URL/api/v1/tenants/onboard"
check_service "Anupalan Rule Service (8082)" "$ANUPALAN_URL/api/v1/anupalan/rules/categories"
check_service "Task Service (8084)" "$TASK_URL/api/v1/anupalan/score?tenantId=00000000-0000-0000-0000-000000000000"

echo ""
echo "If any service is not running, start it with:"
echo "  cd <module-dir> && ../mvnw spring-boot:run"
echo ""

# =============================================================================
# STEP 1: Onboard Tenant 1 - Sharma Plastics (Greater Noida, UP)
# =============================================================================
print_header "STEP 1: Onboard Tenant - Sharma Plastics Pvt Ltd"

print_step "Creating tenant via GSTIN onboarding..."
TENANT1_RESPONSE=$(curl -s -X POST "$PROFILE_URL/api/v1/tenants/onboard" \
  -H "Content-Type: application/json" \
  -d '{
    "gstin": "09AABCS1429B1ZX",
    "udyam": "UDYAM-UP-09-0012345",
    "companyName": "Sharma Plastics Pvt Ltd",
    "nicCode": "2220",
    "state": "UP",
    "district": "Gautam Buddha Nagar",
    "industryCategory": "ORANGE",
    "preferredLanguage": "hi",
    "ownerName": "Rajesh Sharma",
    "ownerPhone": "9876543210"
  }')

echo "$TENANT1_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$TENANT1_RESPONSE"
TENANT1_ID=$(echo "$TENANT1_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")

if [ -n "$TENANT1_ID" ]; then
    print_success "Tenant 1 created: $TENANT1_ID"
else
    print_error "Failed to create Tenant 1. Response: $TENANT1_RESPONSE"
    echo "Trying to extract ID from response..."
    TENANT1_ID=$(echo "$TENANT1_RESPONSE" | grep -o '"id":"[^"]*"' | head -1 | cut -d'"' -f4)
    echo "Extracted ID: $TENANT1_ID"
fi

# =============================================================================
# STEP 2: Onboard Tenant 2 - Gupta Textiles (Okhla, Delhi)
# =============================================================================
print_header "STEP 2: Onboard Tenant - Gupta Textiles & Dyeing"

print_step "Creating tenant via GSTIN onboarding..."
TENANT2_RESPONSE=$(curl -s -X POST "$PROFILE_URL/api/v1/tenants/onboard" \
  -H "Content-Type: application/json" \
  -d '{
    "gstin": "07AABCG5678K1Z5",
    "udyam": "UDYAM-DL-07-0098765",
    "companyName": "Gupta Textiles & Dyeing",
    "nicCode": "1313",
    "state": "DL",
    "district": "South East Delhi",
    "industryCategory": "RED",
    "preferredLanguage": "hi",
    "ownerName": "Amit Gupta",
    "ownerPhone": "9988776655"
  }')

echo "$TENANT2_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$TENANT2_RESPONSE"
TENANT2_ID=$(echo "$TENANT2_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")

if [ -n "$TENANT2_ID" ]; then
    print_success "Tenant 2 created: $TENANT2_ID"
else
    print_error "Failed to create Tenant 2"
fi

# =============================================================================
# STEP 3: Add Floor Manager user to Tenant 1
# =============================================================================
print_header "STEP 3: Add Floor Manager to Sharma Plastics"

if [ -n "$TENANT1_ID" ]; then
    print_step "Adding Floor Manager..."
    FLOOR_MGR_RESPONSE=$(curl -s -X POST "$PROFILE_URL/api/v1/tenants/$TENANT1_ID/users" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "Suresh Kumar",
        "phone": "9123456789",
        "role": "FLOOR_MANAGER"
      }')

    echo "$FLOOR_MGR_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$FLOOR_MGR_RESPONSE"
    FLOOR_MGR_ID=$(echo "$FLOOR_MGR_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")
    print_success "Floor Manager added: $FLOOR_MGR_ID"

    print_step "Adding Compliance Consultant..."
    CONSULTANT_RESPONSE=$(curl -s -X POST "$PROFILE_URL/api/v1/tenants/$TENANT1_ID/users" \
      -H "Content-Type: application/json" \
      -d '{
        "name": "CA Priya Verma",
        "phone": "9876501234",
        "role": "CONSULTANT"
      }')

    echo "$CONSULTANT_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$CONSULTANT_RESPONSE"
    print_success "Consultant added"
fi

# =============================================================================
# STEP 4: List Users for Tenant 1
# =============================================================================
print_header "STEP 4: List Users for Sharma Plastics"

if [ -n "$TENANT1_ID" ]; then
    print_step "Fetching users..."
    curl -s "$PROFILE_URL/api/v1/tenants/$TENANT1_ID/users" | python3 -m json.tool 2>/dev/null
fi

# =============================================================================
# STEP 5: Get Tenant Profile
# =============================================================================
print_header "STEP 5: Get Tenant Profile"

if [ -n "$TENANT1_ID" ]; then
    print_step "Fetching Sharma Plastics profile..."
    curl -s "$PROFILE_URL/api/v1/tenants/$TENANT1_ID" | python3 -m json.tool 2>/dev/null
fi

# =============================================================================
# STEP 6: Get Applicable Anupalan Rules
# =============================================================================
print_header "STEP 6: Query Anupalan Rules for Plastics (NIC 2220) in UP"

print_step "Fetching applicable compliance rules..."
curl -s "$ANUPALAN_URL/api/v1/anupalan/rules/applicable?nicCode=2220&state=UP" | python3 -m json.tool 2>/dev/null

print_step "Fetching all compliance categories..."
curl -s "$ANUPALAN_URL/api/v1/anupalan/rules/categories" | python3 -m json.tool 2>/dev/null

# =============================================================================
# STEP 7: Create Compliance Tasks for Tenant 1
# =============================================================================
print_header "STEP 7: Create Compliance Tasks for Sharma Plastics"

if [ -n "$TENANT1_ID" ]; then
    print_step "Creating task: UPPCB CTO Renewal..."
    TASK1_RESPONSE=$(curl -s -X POST "$TASK_URL/api/v1/tasks" \
      -H "Content-Type: application/json" \
      -d "{
        \"tenantId\": \"$TENANT1_ID\",
        \"assignedTo\": \"$FLOOR_MGR_ID\",
        \"title\": \"UPPCB Consent to Operate (CTO) - Renewal\",
        \"description\": \"CTO renewal due for Sharma Plastics. Submit application on OCMMS portal with required documents.\",
        \"category\": \"POLLUTION\",
        \"status\": \"PENDING\",
        \"dueDate\": \"2026-04-25\"
      }" 2>/dev/null)

    echo "$TASK1_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$TASK1_RESPONSE"
    TASK1_ID=$(echo "$TASK1_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")
    print_success "Task 1 created: $TASK1_ID"

    print_step "Creating task: Fire NOC Renewal..."
    TASK2_RESPONSE=$(curl -s -X POST "$TASK_URL/api/v1/tasks" \
      -H "Content-Type: application/json" \
      -d "{
        \"tenantId\": \"$TENANT1_ID\",
        \"assignedTo\": \"$FLOOR_MGR_ID\",
        \"title\": \"Fire NOC - Annual Renewal\",
        \"description\": \"Fire NOC expires next month. Get fire safety inspection done and submit renewal application.\",
        \"category\": \"FIRE\",
        \"status\": \"PENDING\",
        \"dueDate\": \"2026-04-10\"
      }" 2>/dev/null)

    echo "$TASK2_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$TASK2_RESPONSE"
    TASK2_ID=$(echo "$TASK2_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")
    print_success "Task 2 created: $TASK2_ID"

    print_step "Creating task: Factory License Renewal..."
    TASK3_RESPONSE=$(curl -s -X POST "$TASK_URL/api/v1/tasks" \
      -H "Content-Type: application/json" \
      -d "{
        \"tenantId\": \"$TENANT1_ID\",
        \"assignedTo\": \"$FLOOR_MGR_ID\",
        \"title\": \"Factory License - Annual Renewal\",
        \"description\": \"Factory license renewal under Factories Act 1948. Prepare worker register, safety audit report.\",
        \"category\": \"LABOUR\",
        \"status\": \"PENDING\",
        \"dueDate\": \"2026-05-15\"
      }" 2>/dev/null)

    echo "$TASK3_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$TASK3_RESPONSE"
    TASK3_ID=$(echo "$TASK3_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")
    print_success "Task 3 created: $TASK3_ID"

    print_step "Creating an OVERDUE task for Anupalan Score testing..."
    TASK4_RESPONSE=$(curl -s -X POST "$TASK_URL/api/v1/tasks" \
      -H "Content-Type: application/json" \
      -d "{
        \"tenantId\": \"$TENANT1_ID\",
        \"assignedTo\": \"$FLOOR_MGR_ID\",
        \"title\": \"Hazardous Waste Return - Quarterly Submission\",
        \"description\": \"Quarterly hazardous waste return overdue. Submit Form 4 on UPPCB OCMMS portal.\",
        \"category\": \"POLLUTION\",
        \"status\": \"OVERDUE\",
        \"dueDate\": \"2026-03-01\"
      }" 2>/dev/null)

    echo "$TASK4_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$TASK4_RESPONSE"
    TASK4_ID=$(echo "$TASK4_RESPONSE" | python3 -c "import sys,json; print(json.load(sys.stdin)['id'])" 2>/dev/null || echo "")
    print_success "Task 4 (Overdue) created: $TASK4_ID"
fi

# =============================================================================
# STEP 8: Get Tasks for Tenant
# =============================================================================
print_header "STEP 8: List All Tasks for Sharma Plastics"

if [ -n "$TENANT1_ID" ]; then
    print_step "All tasks..."
    curl -s "$TASK_URL/api/v1/tasks?tenantId=$TENANT1_ID" | python3 -m json.tool 2>/dev/null

    print_step "Only PENDING tasks..."
    curl -s "$TASK_URL/api/v1/tasks?tenantId=$TENANT1_ID&status=PENDING" | python3 -m json.tool 2>/dev/null

    print_step "Only OVERDUE tasks..."
    curl -s "$TASK_URL/api/v1/tasks?tenantId=$TENANT1_ID&status=OVERDUE" | python3 -m json.tool 2>/dev/null
fi

# =============================================================================
# STEP 9: Get Anupalan Score
# =============================================================================
print_header "STEP 9: Calculate Anupalan Score for Sharma Plastics"

if [ -n "$TENANT1_ID" ]; then
    print_step "Fetching Anupalan Score..."
    curl -s "$TASK_URL/api/v1/anupalan/score?tenantId=$TENANT1_ID" | python3 -m json.tool 2>/dev/null

    print_step "Fetching Anupalan Dashboard..."
    curl -s "$TASK_URL/api/v1/anupalan/dashboard?tenantId=$TENANT1_ID" | python3 -m json.tool 2>/dev/null
fi

# =============================================================================
# STEP 10: Update Task Status
# =============================================================================
print_header "STEP 10: Update Task Status - Mark Fire NOC as COMPLETED"

if [ -n "$TASK2_ID" ]; then
    print_step "Completing Fire NOC task..."
    curl -s -X PUT "$TASK_URL/api/v1/tasks/$TASK2_ID/status?status=COMPLETED" | python3 -m json.tool 2>/dev/null
    print_success "Fire NOC task marked as COMPLETED"

    print_step "Recalculating Anupalan Score after completion..."
    curl -s "$TASK_URL/api/v1/anupalan/score?tenantId=$TENANT1_ID" | python3 -m json.tool 2>/dev/null
fi

# =============================================================================
# STEP 11: Reschedule a Task
# =============================================================================
print_header "STEP 11: Reschedule CTO Renewal Task"

if [ -n "$TASK1_ID" ]; then
    print_step "Rescheduling CTO renewal to May 15..."
    curl -s -X PUT "$TASK_URL/api/v1/tasks/$TASK1_ID/reschedule?newDate=2026-05-15&reason=Waiting%20for%20lab%20report%20from%20NABL%20accredited%20lab" | python3 -m json.tool 2>/dev/null
    print_success "CTO renewal rescheduled"
fi

# =============================================================================
# STEP 12: Acknowledge a Task Alert
# =============================================================================
print_header "STEP 12: Acknowledge Task Alert (Kavach escalation prevention)"

if [ -n "$TASK3_ID" ]; then
    print_step "Acknowledging Factory License task..."
    curl -s -X POST "$TASK_URL/api/v1/tasks/$TASK3_ID/acknowledge" | python3 -m json.tool 2>/dev/null
    print_success "Task acknowledged - Kavach will not escalate"
fi

# =============================================================================
# STEP 13: Update Tenant Profile
# =============================================================================
print_header "STEP 13: Update Tenant Profile"

if [ -n "$TENANT1_ID" ]; then
    print_step "Changing preferred language to English..."
    curl -s -X PUT "$PROFILE_URL/api/v1/tenants/$TENANT1_ID" \
      -H "Content-Type: application/json" \
      -d '{
        "preferredLanguage": "en",
        "district": "Greater Noida"
      }' | python3 -m json.tool 2>/dev/null
    print_success "Profile updated"
fi

# =============================================================================
# STEP 14: Test via API Gateway (port 8090)
# =============================================================================
print_header "STEP 14: Test Requests via Niyamitra API Gateway (8090)"

if [ -n "$TENANT1_ID" ]; then
    print_step "GET tenant via Gateway..."
    curl -s "$GATEWAY_URL/api/v1/tenants/$TENANT1_ID" | python3 -m json.tool 2>/dev/null

    print_step "GET Anupalan rules via Gateway..."
    curl -s "$GATEWAY_URL/api/v1/anupalan/rules/categories" | python3 -m json.tool 2>/dev/null

    print_step "GET tasks via Gateway..."
    curl -s "$GATEWAY_URL/api/v1/tasks?tenantId=$TENANT1_ID" | python3 -m json.tool 2>/dev/null

    print_step "GET Anupalan score via Gateway..."
    curl -s "$GATEWAY_URL/api/v1/anupalan/score?tenantId=$TENANT1_ID" | python3 -m json.tool 2>/dev/null
fi

# =============================================================================
# STEP 15: Test Kavach WhatsApp Webhook Verification
# =============================================================================
print_header "STEP 15: Test Kavach WhatsApp Webhook"

print_step "Testing webhook verification (GET)..."
curl -s "$KAVACH_URL/api/v1/kavach/whatsapp/webhook?hub.mode=subscribe&hub.verify_token=kavach-verify-token&hub.challenge=test_challenge_123"
echo ""

print_step "Simulating incoming WhatsApp message (POST)..."
curl -s -X POST "$KAVACH_URL/api/v1/kavach/whatsapp/webhook" \
  -H "Content-Type: application/json" \
  -d '{
    "entry": [{
      "changes": [{
        "value": {
          "messages": [{
            "from": "919876543210",
            "type": "text",
            "text": {
              "body": "Namaste Kavach, mera CTO renewal ka kya status hai?"
            }
          }]
        }
      }]
    }]
  }'
echo ""
print_success "WhatsApp webhook test complete"

# =============================================================================
# SUMMARY
# =============================================================================
print_header "SETUP COMPLETE - Summary"

echo ""
echo -e "${GREEN}Tenants Created:${NC}"
echo "  1. Sharma Plastics Pvt Ltd (GSTIN: 09AABCS1429B1ZX) - ID: $TENANT1_ID"
echo "  2. Gupta Textiles & Dyeing (GSTIN: 07AABCG5678K1Z5) - ID: $TENANT2_ID"
echo ""
echo -e "${GREEN}Users Created (Tenant 1):${NC}"
echo "  1. Rajesh Sharma (Owner)        - Phone: 9876543210"
echo "  2. Suresh Kumar (Floor Manager)  - Phone: 9123456789"
echo "  3. CA Priya Verma (Consultant)   - Phone: 9876501234"
echo ""
echo -e "${GREEN}Tasks Created (Tenant 1):${NC}"
echo "  1. UPPCB CTO Renewal          - PENDING   - Due: 2026-05-15 (rescheduled)"
echo "  2. Fire NOC Renewal            - COMPLETED"
echo "  3. Factory License Renewal     - PENDING   - Due: 2026-05-15 (acknowledged)"
echo "  4. Hazardous Waste Return      - OVERDUE   - Due: 2026-03-01"
echo ""
echo -e "${GREEN}Anupalan Compliance Rules Seeded:${NC}"
echo "  1. UPPCB Consent to Operate (CTO)"
echo "  2. Fire NOC"
echo "  3. Factory License"
echo "  4. Hazardous Waste Authorization"
echo "  5. Boiler Registration"
echo "  6. UPPCB Consent to Establish (CTE)"
echo ""
echo -e "${BLUE}=============================================================================${NC}"
echo -e "${BLUE}  Quick Reference - Individual cURL Commands${NC}"
echo -e "${BLUE}=============================================================================${NC}"
echo ""
echo "# --- PROFILE SERVICE (8081) ---"
echo "# Onboard new tenant"
echo 'curl -X POST http://localhost:8081/api/v1/tenants/onboard -H "Content-Type: application/json" -d '"'"'{"gstin":"09AABCS1429B1ZX","companyName":"Test Factory","nicCode":"2220","state":"UP","ownerName":"Test Owner","ownerPhone":"9876543210"}'"'"''
echo ""
echo "# Get tenant profile"
echo "curl http://localhost:8081/api/v1/tenants/{tenantId}"
echo ""
echo "# Update tenant"
echo 'curl -X PUT http://localhost:8081/api/v1/tenants/{tenantId} -H "Content-Type: application/json" -d '"'"'{"preferredLanguage":"en"}'"'"''
echo ""
echo "# Add user to tenant"
echo 'curl -X POST http://localhost:8081/api/v1/tenants/{tenantId}/users -H "Content-Type: application/json" -d '"'"'{"name":"Test User","phone":"9123456789","role":"FLOOR_MANAGER"}'"'"''
echo ""
echo "# List users"
echo "curl http://localhost:8081/api/v1/tenants/{tenantId}/users"
echo ""
echo "# --- ANUPALAN RULE SERVICE (8082) ---"
echo "# Get applicable rules for industry + state"
echo "curl 'http://localhost:8082/api/v1/anupalan/rules/applicable?nicCode=2220&state=UP'"
echo ""
echo "# Get rule details"
echo "curl http://localhost:8082/api/v1/anupalan/rules/{ruleId}"
echo ""
echo "# List all categories"
echo "curl http://localhost:8082/api/v1/anupalan/rules/categories"
echo ""
echo "# --- TASK SERVICE (8084) ---"
echo "# List tasks"
echo "curl 'http://localhost:8084/api/v1/tasks?tenantId={tenantId}'"
echo "curl 'http://localhost:8084/api/v1/tasks?tenantId={tenantId}&status=PENDING'"
echo ""
echo "# Get task details"
echo "curl http://localhost:8084/api/v1/tasks/{taskId}"
echo ""
echo "# Update task status"
echo "curl -X PUT 'http://localhost:8084/api/v1/tasks/{taskId}/status?status=COMPLETED'"
echo ""
echo "# Reschedule task"
echo "curl -X PUT 'http://localhost:8084/api/v1/tasks/{taskId}/reschedule?newDate=2026-06-01&reason=Delayed'"
echo ""
echo "# Acknowledge task alert"
echo "curl -X POST http://localhost:8084/api/v1/tasks/{taskId}/acknowledge"
echo ""
echo "# Anupalan Score"
echo "curl 'http://localhost:8084/api/v1/anupalan/score?tenantId={tenantId}'"
echo ""
echo "# Anupalan Dashboard"
echo "curl 'http://localhost:8084/api/v1/anupalan/dashboard?tenantId={tenantId}'"
echo ""
echo "# --- DOCUMENT VAULT (8083) ---"
echo "# Upload document"
echo "curl -X POST 'http://localhost:8083/api/v1/documents/upload?tenantId={tenantId}&uploadSource=NIYAMITRA_WEB' -F 'file=@/path/to/file.pdf'"
echo ""
echo "# Get document"
echo "curl http://localhost:8083/api/v1/documents/{docId}"
echo ""
echo "# Get download URL"
echo "curl http://localhost:8083/api/v1/documents/{docId}/download"
echo ""
echo "# List documents"
echo "curl 'http://localhost:8083/api/v1/documents?tenantId={tenantId}'"
echo ""
echo "# --- KAVACH WHATSAPP (8086) ---"
echo "# Verify webhook"
echo "curl 'http://localhost:8086/api/v1/kavach/whatsapp/webhook?hub.mode=subscribe&hub.verify_token=kavach-verify-token&hub.challenge=test123'"
echo ""
echo "# Simulate incoming message"
echo 'curl -X POST http://localhost:8086/api/v1/kavach/whatsapp/webhook -H "Content-Type: application/json" -d '"'"'{"entry":[{"changes":[{"value":{"messages":[{"from":"919876543210","type":"text","text":{"body":"Hello Kavach"}}]}}]}]}'"'"''
echo ""
echo "# --- VIA API GATEWAY (8090) ---"
echo "# All above endpoints are also available via Gateway at port 8090"
echo "curl http://localhost:8090/api/v1/tenants/{tenantId}"
echo "curl 'http://localhost:8090/api/v1/anupalan/rules/categories'"
echo "curl 'http://localhost:8090/api/v1/tasks?tenantId={tenantId}'"
echo ""
