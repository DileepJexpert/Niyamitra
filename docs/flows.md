# Niyamitra Event Flow Diagrams

This document contains detailed sequence diagrams for every business flow in the Niyamitra platform.

## Table of Contents

1. [Tenant Onboarding Flow](#1-tenant-onboarding-flow)
2. [Document Upload → Extraction Flow](#2-document-upload--extraction-flow)
3. [Task Expiry → Notification Flow](#3-task-expiry--notification-flow)
4. [WhatsApp Conversation Flow (Kavach AI)](#4-whatsapp-conversation-flow-kavach-ai)
5. [Escalation Flow](#5-escalation-flow)
6. [Dashboard Data Flow](#6-dashboard-data-flow)
7. [Anupalan Score Calculation](#7-anupalan-score-calculation)
8. [Gazette Watcher Flow (Phase 2)](#8-gazette-watcher-flow-phase-2)

---

## 1. Tenant Onboarding Flow

**Trigger:** New factory owner signs up (via dashboard or onboarding API)

**Outcome:** Tenant created, compliance tasks auto-generated based on industry & state

```mermaid
sequenceDiagram
    actor User as Factory Owner
    participant DASH as Dashboard
    participant GW as API Gateway
    participant PS as Profile Service
    participant PG as PostgreSQL
    participant K as Kafka
    participant ARS as Anupalan Rule Service
    participant TS as Task Service

    User->>DASH: Fill onboarding form
    DASH->>GW: POST /api/v1/tenants/onboard
    GW->>PS: Forward request
    PS->>PG: INSERT tenant + owner user
    PG-->>PS: Tenant ID
    PS->>K: Publish TenantOnboardedEvent
    PS-->>GW: 201 Created
    GW-->>DASH: Tenant created
    DASH-->>User: Welcome screen

    Note over K,ARS: Async processing begins
    K->>ARS: TenantOnboardedEvent
    ARS->>PG: SELECT rules WHERE industry & state match
    PG-->>ARS: Applicable rules (e.g., 6 rules)
    ARS->>K: Publish TasksGeneratedEvent
    K->>TS: TasksGeneratedEvent
    TS->>PG: INSERT compliance_tasks (bulk)
    PG-->>TS: OK

    Note over TS: Tasks now visible in dashboard<br/>& accessible by Kavach agent
```

**Key Files:**
- `niyamitra-profile-service/TenantService.java` — Publishes `TenantOnboardedEvent`
- `anupalan-rule-service/TenantOnboardedConsumer.java` — Consumes event, generates tasks
- `niyamitra-task-service/TaskEventConsumer.java` — Creates tasks in DB

---

## 2. Document Upload → Extraction Flow

**Trigger:** User uploads document via Dashboard, WhatsApp (Kavach), or email

**Outcome:** Document stored, Kavach Vision extracts data, task status updated if applicable

```mermaid
sequenceDiagram
    actor User
    participant CH as Channel<br/>(Dashboard/WA/Email)
    participant DV as Document Vault
    participant MINIO as MinIO
    participant PG as PostgreSQL
    participant K as Kafka
    participant KV as Kavach Vision
    participant TS as Task Service
    participant KFM as Kavach Floor Manager

    User->>CH: Upload compliance document
    CH->>DV: POST /api/v1/documents/upload
    DV->>MINIO: Upload file (tenantId/filename)
    MINIO-->>DV: S3 key
    DV->>PG: INSERT document (status=UPLOADED)
    PG-->>DV: Document ID
    DV->>K: Publish DocumentUploadedEvent
    DV-->>CH: 201 Created
    CH-->>User: Upload success

    Note over K,KV: Async extraction pipeline
    K->>KV: DocumentUploadedEvent
    KV->>PG: UPDATE status=PROCESSING
    KV->>KV: Extract metadata<br/>(category, dates, doc type)
    KV->>PG: UPDATE extraction_result JSONB
    KV->>PG: UPDATE status=EXTRACTED
    KV->>K: Publish DocumentExtractedEvent

    par Task Service reacts
        K->>TS: DocumentExtractedEvent
        TS->>PG: Link document to task<br/>Mark task IN_PROGRESS
    and Kavach confirms to user
        K->>KFM: DocumentExtractedEvent
        KFM->>K: Publish KavachWhatsAppSendEvent<br/>(confirmation message)
    end
```

**Key Files:**
- `niyamitra-document-vault/DocumentService.java` — Upload + publish event
- `niyamitra-document-vault/KavachVisionService.java` — Metadata extraction (Phase 2)
- `niyamitra-task-service/TaskEventConsumer.java` — Consumes `DocumentExtractedEvent`

---

## 3. Task Expiry → Notification Flow

**Trigger:** Daily cron at 06:00 IST scans all active tasks

**Outcome:** Users receive WhatsApp reminders at T-30, T-15, T-5 days before deadline

```mermaid
sequenceDiagram
    participant CRON as Expiry Scheduler<br/>(06:00 IST daily)
    participant TS as Task Service
    participant PG as PostgreSQL
    participant K as Kafka
    participant NS as Notification Service
    participant PS as Profile Service
    participant WA as Meta WhatsApp<br/>Cloud API
    actor User

    CRON->>TS: Trigger expiry check
    TS->>PG: SELECT tasks WHERE status != COMPLETED<br/>AND due_date - now IN (30, 15, 5) days
    PG-->>TS: Upcoming tasks

    loop For each task
        TS->>K: Publish ExpiryApproachingEvent<br/>(T-30 / T-15 / T-5)
    end

    K->>NS: ExpiryApproachingEvent
    NS->>PS: GET user phone (REST)
    PS-->>NS: phone, language
    NS->>NS: Build WhatsApp template<br/>(Hindi/English)
    NS->>WA: Send WhatsApp message
    WA-->>NS: 200 OK + message ID
    NS->>PG: INSERT kavach_notification

    WA->>User: WhatsApp reminder delivered
    Note over User: "Aapki UPPCB CTO<br/>15 din mein expire ho rahi hai"
```

**Key Files:**
- `niyamitra-task-service/ExpiryCheckScheduler.java` — Daily cron
- `niyamitra-task-service/TaskService.java` — Publishes `ExpiryApproachingEvent`
- `kavach-notification-service/KavachEventConsumer.java` — Handles expiry events
- `kavach-notification-service/WhatsAppService.java` — Meta Cloud API client

---

## 4. WhatsApp Conversation Flow (Kavach AI)

**Trigger:** User sends WhatsApp message to Kavach

**Outcome:** LLM agent responds with contextual answer using @Tool function calling

```mermaid
sequenceDiagram
    actor User as Factory Owner
    participant WA as Meta WhatsApp
    participant WHOOK as Webhook Controller<br/>(floor-manager)
    participant K as Kafka
    participant KMC as Kavach Message Consumer
    participant AGT as KavachAgent<br/>(LangChain4j)
    participant CLAUDE as Anthropic Claude
    participant TOOLS as KavachTools
    participant TS as Task Service
    participant DV as Document Vault
    participant NS as Notification Service

    User->>WA: "Mere aane wale task dikhao"<br/>(Show my upcoming tasks)
    WA->>WHOOK: POST /api/v1/kavach/webhook
    WHOOK->>K: Publish KavachWhatsAppReceivedEvent
    WHOOK-->>WA: 200 OK

    K->>KMC: KavachWhatsAppReceivedEvent
    KMC->>AGT: agent.chat(userId, message)
    AGT->>CLAUDE: Send message + tool definitions

    CLAUDE-->>AGT: Tool call: getUpcomingTasks(tenantId)
    AGT->>TOOLS: getUpcomingTasks()
    TOOLS->>TS: GET /api/v1/tasks?tenantId=...
    TS-->>TOOLS: Task list
    TOOLS-->>AGT: Formatted task summary

    AGT->>CLAUDE: Tool result
    CLAUDE-->>AGT: Final response in Hindi

    AGT-->>KMC: Response text
    KMC->>K: Publish KavachWhatsAppSendEvent
    K->>NS: KavachWhatsAppSendEvent
    NS->>WA: Send reply message
    WA->>User: "Aapke paas 3 task hain:<br/>1. Fire NOC - 12 din<br/>2. UPPCB CTO - 25 din<br/>3. ..."
```

**Available @Tool Functions:**

| Tool | Description |
|------|-------------|
| `getUpcomingTasks` | List tasks due in next 30 days |
| `getTaskDetails` | Full details for a specific task |
| `rescheduleTask` | Move due date with reason |
| `markDocumentReceived` | Link uploaded doc to task |
| `escalateToOwner` | Publish escalation event |
| `searchAnupalanRules` | Query rule database |

**Key Files:**
- `kavach-floor-manager/WhatsAppWebhookController.java`
- `kavach-floor-manager/KavachMessageConsumer.java`
- `kavach-floor-manager/KavachAgent.java` (LangChain4j `@AiService`)
- `kavach-floor-manager/KavachTools.java` (6 `@Tool` methods)

---

## 5. Escalation Flow

**Trigger:** Task is 5 days overdue AND not acknowledged by assigned user

**Outcome:** Owner receives escalation WhatsApp; task level incremented

```mermaid
sequenceDiagram
    participant CRON as Task Scheduler
    participant TS as Task Service
    participant PG as PostgreSQL
    participant K as Kafka
    participant NS as Notification Service
    participant PS as Profile Service
    participant WA as WhatsApp
    actor FM as Floor Manager
    actor OWN as Owner

    CRON->>TS: Daily escalation check
    TS->>PG: SELECT tasks WHERE due_date <= now - 5 days<br/>AND acknowledged = false<br/>AND escalation_level < MAX
    PG-->>TS: Unacknowledged overdue tasks

    loop For each task
        TS->>PG: UPDATE escalation_level + 1<br/>status = ESCALATED
        TS->>K: Publish EscalationTriggeredEvent
    end

    K->>NS: EscalationTriggeredEvent (L1)
    NS->>PS: GET owner user for tenant
    PS-->>NS: owner phone
    NS->>WA: Send escalation message
    WA->>OWN: "URGENT: Task overdue 5 din<br/>assigned to [FM name]"

    Note over OWN: Owner reviews in dashboard<br/>or replies via WhatsApp

    alt Owner acknowledges
        OWN->>WA: "Theek hai, main dekh leta hun"
        WA->>NS: (via Kavach agent flow)
        NS->>TS: PUT /tasks/{id}/acknowledge
        TS->>PG: UPDATE acknowledged = true
    else Still unresolved at T-10 days
        CRON->>TS: Next check
        TS->>K: Publish EscalationTriggeredEvent (L2)
        NS->>OWN: Stronger reminder
    end
```

**Key Files:**
- `niyamitra-task-service/TaskService.java` — `checkAndEscalate()` logic
- `niyamitra-task-service/ExpiryCheckScheduler.java`
- `kavach-notification-service/KavachEventConsumer.java` — `onEscalation()`

---

## 6. Dashboard Data Flow

**Trigger:** User opens Next.js dashboard at `/dashboard`

**Outcome:** Dashboard renders Anupalan Score, task charts, upcoming deadlines

```mermaid
sequenceDiagram
    actor User
    participant NEXT as Next.js Dashboard<br/>:3000
    participant GW as API Gateway<br/>:8090
    participant TS as Task Service
    participant PS as Profile Service
    participant DV as Document Vault
    participant NS as Notification Service
    participant PG as PostgreSQL

    User->>NEXT: Navigate to /dashboard
    NEXT->>NEXT: useApi hooks trigger

    par Fetch tasks
        NEXT->>GW: GET /api/v1/tasks?tenantId=...
        GW->>TS: Forward
        TS->>PG: SELECT tasks
        PG-->>TS: Task list
        TS-->>GW: JSON
        GW-->>NEXT: Tasks
    and Fetch Anupalan Score
        NEXT->>GW: GET /api/v1/tasks/anupalan-score?tenantId=...
        GW->>TS: Forward
        TS->>PG: Calculate score<br/>(weighted formula)
        PG-->>TS: Task stats
        TS-->>GW: Score JSON
        GW-->>NEXT: Score
    end

    NEXT->>NEXT: Render components:<br/>AnupalanScoreGauge<br/>TaskDistributionChart<br/>UpcomingDeadlines<br/>ComplianceTrend
    NEXT-->>User: Dashboard displayed
```

**Anupalan Score Formula:**
```
Score = 100 - (0.4 × OverdueRatio × 100
             + 0.3 × LateSubmissionRatio × 100
             + 0.2 × PendingRatio × 100
             + 0.1 × ViolationCount × 10)
```

**Key Files:**
- `niyamitra-dashboard/src/app/dashboard/page.tsx` — Dashboard page
- `niyamitra-dashboard/src/lib/api.ts` — API client
- `niyamitra-task-service/AnupalanScoreService.java` — Score calculation

---

## 7. Anupalan Score Calculation

```mermaid
flowchart TB
    START([Dashboard requests score]) --> FETCH[Fetch all tenant tasks]
    FETCH --> COUNT[Count by status]
    COUNT --> CALC{Calculate ratios}

    CALC --> R1[OverdueRatio =<br/>overdue / total]
    CALC --> R2[LateSubmissionRatio =<br/>completed late / completed]
    CALC --> R3[PendingRatio =<br/>pending / total]
    CALC --> R4[ViolationCount =<br/>escalated > L2]

    R1 --> WEIGHT
    R2 --> WEIGHT
    R3 --> WEIGHT
    R4 --> WEIGHT

    WEIGHT[Apply weights:<br/>0.4 + 0.3 + 0.2 + 0.1] --> SCORE[Score = 100 - weighted penalty]
    SCORE --> CLAMP[Clamp 0..100]
    CLAMP --> COLOR{Score range}

    COLOR -->|>= 80| GREEN[Green: Excellent]
    COLOR -->|60-79| YELLOW[Yellow: Good]
    COLOR -->|40-59| ORANGE[Orange: Needs attention]
    COLOR -->|< 40| RED[Red: Critical]

    GREEN --> END([Return to dashboard])
    YELLOW --> END
    ORANGE --> END
    RED --> END

    style START fill:#2563eb,color:#fff
    style GREEN fill:#22c55e,color:#fff
    style YELLOW fill:#eab308,color:#fff
    style ORANGE fill:#f97316,color:#fff
    style RED fill:#ef4444,color:#fff
```

---

## 8. Gazette Watcher Flow (Phase 2)

**Trigger:** Scheduled crawler runs every 6 hours

**Outcome:** New regulatory updates trigger notifications to affected tenants

```mermaid
sequenceDiagram
    participant CRON as Gazette Scheduler<br/>(every 6h)
    participant GW as Gazette Watcher Agent
    participant SOURCES as Gazette Sources<br/>(CPCB, State PCBs, MoLE)
    participant CLAUDE as Claude<br/>(relevance scoring)
    participant K as Kafka
    participant PG as PostgreSQL
    participant NS as Notification Service
    participant PS as Profile Service
    actor Users as Affected Tenants

    CRON->>GW: Start crawl
    GW->>SOURCES: Fetch new notifications
    SOURCES-->>GW: HTML/PDF documents

    GW->>GW: Parse + extract text
    GW->>CLAUDE: Analyze: category, states, industries
    CLAUDE-->>GW: Structured metadata

    GW->>PG: INSERT anupalan_gazette_notifications
    GW->>K: Publish GazetteFoundEvent

    K->>NS: GazetteFoundEvent
    NS->>PS: GET tenants WHERE<br/>industry + state matches
    PS-->>NS: Affected tenants list

    loop For each affected tenant
        NS->>NS: Build WhatsApp alert
        NS->>Users: Send gazette notification
    end

    Users->>Users: "New UPPCB rule:<br/>Zero Liquid Discharge<br/>effective 01 Jan 2027"
```

**Note:** Gazette Watcher is a Phase 2 deliverable — skeleton will be added next.

---

## Cross-References

- [Architecture Overview](./architecture.md) — System topology and components
- [README](../README.md) — Getting started guide
