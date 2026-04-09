# Niyamitra Architecture

This document describes the complete system architecture of the Niyamitra compliance platform across all phases.

## Table of Contents

1. [Brand Hierarchy](#brand-hierarchy)
2. [High-Level Architecture](#high-level-architecture)
3. [Microservices Map](#microservices-map)
4. [Data Architecture](#data-architecture)
5. [Event-Driven Architecture](#event-driven-architecture)
6. [Infrastructure Stack](#infrastructure-stack)
7. [Deployment Topology](#deployment-topology)

---

## Brand Hierarchy

Niyamitra follows a three-tier brand architecture:

```mermaid
graph TB
    N[Niyamitra<br/>Parent Brand<br/>Company Identity]
    K[Kavach AI<br/>WhatsApp Agent Engine]
    A[Anupalan<br/>Compliance Intelligence<br/>& Risk Scoring]

    N --> K
    N --> A

    style N fill:#1e3a8a,color:#fff
    style K fill:#15803d,color:#fff
    style A fill:#a16207,color:#fff
```

| Brand | Role | Examples |
|-------|------|----------|
| **Niyamitra** | Parent brand, core platform | `niyamitra-profile-service`, `niyamitra-tasks`, `niyamitra-dashboard` |
| **Kavach** | AI agents + WhatsApp interface | `kavach-floor-manager`, `kavach-notification-service`, Kavach Vision |
| **Anupalan** | Compliance rule engine + scoring | `anupalan-rule-service`, Anupalan Score, Anupalan Rules |

---

## High-Level Architecture

```mermaid
graph TB
    subgraph "User Channels"
        WA[WhatsApp<br/>Primary UI]
        DASH[Next.js Dashboard<br/>Secondary UI]
        MOB[React Native App<br/>Phase 4]
    end

    subgraph "Edge Layer"
        META[Meta WhatsApp<br/>Cloud API]
        GW[API Gateway<br/>Port 8090<br/>Rate Limit + CORS]
    end

    subgraph "Application Layer - Spring Boot Microservices"
        PROFILE[Profile Service<br/>:8081]
        RULE[Anupalan Rule Service<br/>:8082]
        DOC[Document Vault<br/>+ Kavach Vision<br/>:8083]
        TASK[Task Service<br/>+ Anupalan Score<br/>:8084]
        NOTIF[Kavach Notification<br/>:8085]
        AGENT[Kavach Floor Manager<br/>AI Agent :8086]
    end

    subgraph "Messaging Layer"
        KAFKA[(Redpanda / Kafka<br/>11 Topics)]
    end

    subgraph "Data Layer"
        PG[(PostgreSQL<br/>5 Schemas)]
        REDIS[(Redis<br/>Cache + RateLimit)]
        MINIO[(MinIO<br/>S3 Storage)]
    end

    subgraph "External Services"
        CLAUDE[Anthropic Claude<br/>LangChain4j]
        KC[Keycloak<br/>Auth]
    end

    WA --> META
    META --> AGENT
    DASH --> GW
    MOB --> GW

    GW --> PROFILE
    GW --> RULE
    GW --> DOC
    GW --> TASK
    GW --> NOTIF
    GW --> AGENT

    PROFILE -.publish.-> KAFKA
    RULE -.consume.-> KAFKA
    RULE -.publish.-> KAFKA
    DOC -.publish.-> KAFKA
    DOC -.consume.-> KAFKA
    TASK -.consume.-> KAFKA
    TASK -.publish.-> KAFKA
    NOTIF -.consume.-> KAFKA
    AGENT -.publish.-> KAFKA
    AGENT -.consume.-> KAFKA

    PROFILE --> PG
    RULE --> PG
    DOC --> PG
    DOC --> MINIO
    TASK --> PG
    NOTIF --> PG

    AGENT --> CLAUDE
    GW --> REDIS
    PROFILE --> KC
    NOTIF --> META

    style WA fill:#25D366,color:#fff
    style DASH fill:#1e40af,color:#fff
    style AGENT fill:#15803d,color:#fff
    style KAFKA fill:#e11d48,color:#fff
    style PG fill:#336791,color:#fff
```

---

## Microservices Map

```mermaid
graph LR
    subgraph "niyamitra-common (Shared Library)"
        COMMON[Events<br/>Enums<br/>Exceptions<br/>Kafka Config]
    end

    subgraph "Phase 1 Services"
        PS[niyamitra-profile-service<br/>Tenants + Users + Credentials]
        ARS[anupalan-rule-service<br/>Compliance Rules]
        DV[niyamitra-document-vault<br/>Documents + MinIO]
        TS[niyamitra-task-service<br/>Tasks + Score + Scheduler]
        KN[kavach-notification-service<br/>WhatsApp Notifications]
        KFM[kavach-floor-manager<br/>LangChain4j AI Agent]
        GW[niyamitra-api-gateway<br/>Routing + Rate Limit]
    end

    subgraph "Phase 2 Additions"
        DASH[niyamitra-dashboard<br/>Next.js 15]
        VIS[Kavach Vision<br/>Document AI<br/>in document-vault]
    end

    COMMON --> PS
    COMMON --> ARS
    COMMON --> DV
    COMMON --> TS
    COMMON --> KN
    COMMON --> KFM

    DASH -.REST.-> GW
    GW -.routes.-> PS
    GW -.routes.-> ARS
    GW -.routes.-> DV
    GW -.routes.-> TS
    GW -.routes.-> KN
    GW -.routes.-> KFM

    VIS -.inside.-> DV

    style COMMON fill:#64748b,color:#fff
    style DASH fill:#1e40af,color:#fff
    style VIS fill:#15803d,color:#fff
```

### Service Responsibilities

| Service | Port | Responsibility | Key Domain |
|---------|------|----------------|------------|
| `niyamitra-api-gateway` | 8090 | Edge routing, CORS, rate limiting | Gateway |
| `niyamitra-profile-service` | 8081 | Tenants, users, portal credentials (AES-256-GCM encrypted) | Identity |
| `anupalan-rule-service` | 8082 | Compliance rules, rule matching, auto task generation | Compliance |
| `niyamitra-document-vault` | 8083 | Document storage (MinIO), Kavach Vision extraction | Documents |
| `niyamitra-task-service` | 8084 | Tasks, Anupalan Score calculation, expiry scheduler | Tasks |
| `kavach-notification-service` | 8085 | WhatsApp notifications, notification history | Notifications |
| `kavach-floor-manager` | 8086 | LangChain4j AI agent, WhatsApp webhook, 6 @Tool methods | AI Agent |
| `niyamitra-dashboard` | 3000 | Next.js 15 web UI (Phase 2) | Web UI |

---

## Data Architecture

### Database Schema Layout (Single PostgreSQL)

```mermaid
erDiagram
    niyamitra_tenants ||--o{ niyamitra_users : "has"
    niyamitra_tenants ||--o{ compliance_tasks : "has"
    niyamitra_tenants ||--o{ niyamitra_documents : "has"
    niyamitra_tenants ||--o{ kavach_portal_credentials : "has"
    niyamitra_tenants ||--o{ kavach_notifications : "has"
    anupalan_rules ||--o{ compliance_tasks : "generates"
    compliance_tasks ||--o{ niyamitra_documents : "references"

    niyamitra_tenants {
        UUID id PK
        string company_name
        string gstin UK
        enum industry_category
        string state
        string district
        string contact_email
        string contact_phone
        timestamp onboarded_at
        boolean active
    }

    niyamitra_users {
        UUID id PK
        UUID tenant_id FK
        string full_name
        string phone
        string email
        enum role
        boolean whatsapp_opt_in
        string preferred_language
    }

    anupalan_rules {
        UUID id PK
        string rule_name
        enum compliance_category
        string applicable_industries
        string applicable_states
        int renewal_frequency_days
        string penalty_description
        boolean active
    }

    compliance_tasks {
        UUID id PK
        UUID tenant_id FK
        UUID rule_id FK
        UUID assigned_user_id FK
        enum status
        date due_date
        timestamp completed_date
        int escalation_level
        boolean acknowledged
    }

    niyamitra_documents {
        UUID id PK
        UUID tenant_id FK
        UUID task_id FK
        string original_filename
        string s3_key
        enum file_type
        bigint file_size_bytes
        enum upload_source
        enum processing_status
        jsonb extraction_result
        timestamp uploaded_at
    }

    kavach_portal_credentials {
        UUID id PK
        UUID tenant_id FK
        string portal_name
        string encrypted_username
        string encrypted_password
        string encryption_version
    }

    kavach_notifications {
        UUID id PK
        UUID tenant_id FK
        UUID user_id FK
        string channel
        string message_type
        text message
        string delivery_status
        timestamp sent_at
    }
```

### Schema Isolation

| Schema | Owner Service | Tables |
|--------|---------------|--------|
| `niyamitra_profiles` | profile-service | `niyamitra_tenants`, `niyamitra_users`, `kavach_portal_credentials` |
| `anupalan_rules` | rule-service | `anupalan_rules` |
| `niyamitra_documents` | document-vault | `niyamitra_documents` |
| `niyamitra_tasks` | task-service | `compliance_tasks` |
| `kavach_notifications` | notification-service | `kavach_notifications`, `anupalan_gazette_notifications` |

Each service owns its own schema — no cross-schema joins. Cross-service queries happen via Kafka events.

---

## Event-Driven Architecture

### Kafka Topic Catalog (11 Topics)

```mermaid
graph LR
    subgraph "niyamitra.* topics"
        T1[niyamitra.tenants.onboarded]
        T2[niyamitra.tasks.generated]
        T3[niyamitra.tasks.expiry-approaching]
        T4[niyamitra.tasks.escalation-triggered]
    end

    subgraph "kavach.* topics"
        K1[kavach.documents.uploaded]
        K2[kavach.documents.extracted]
        K3[kavach.whatsapp.received]
        K4[kavach.whatsapp.send]
    end

    subgraph "anupalan.* topics"
        A1[anupalan.gazette.found]
        A2[anupalan.portal.check-requested]
        A3[anupalan.portal.check-completed]
    end

    style T1 fill:#1e3a8a,color:#fff
    style T2 fill:#1e3a8a,color:#fff
    style T3 fill:#1e3a8a,color:#fff
    style T4 fill:#1e3a8a,color:#fff
    style K1 fill:#15803d,color:#fff
    style K2 fill:#15803d,color:#fff
    style K3 fill:#15803d,color:#fff
    style K4 fill:#15803d,color:#fff
    style A1 fill:#a16207,color:#fff
    style A2 fill:#a16207,color:#fff
    style A3 fill:#a16207,color:#fff
```

### Publish/Subscribe Matrix

| Topic | Publisher | Consumer(s) |
|-------|-----------|-------------|
| `niyamitra.tenants.onboarded` | profile-service | anupalan-rule-service |
| `niyamitra.tasks.generated` | anupalan-rule-service | task-service |
| `niyamitra.tasks.expiry-approaching` | task-service (scheduler) | kavach-notification-service |
| `niyamitra.tasks.escalation-triggered` | task-service, kavach-floor-manager | kavach-notification-service |
| `kavach.documents.uploaded` | document-vault | **Kavach Vision** (in document-vault) |
| `kavach.documents.extracted` | Kavach Vision | task-service |
| `kavach.whatsapp.received` | kavach-floor-manager (webhook) | kavach-floor-manager (agent) |
| `kavach.whatsapp.send` | kavach-floor-manager, anywhere | kavach-notification-service |
| `anupalan.gazette.found` | gazette-watcher (Phase 2) | kavach-notification-service |
| `anupalan.portal.check-requested` | task-service | portal-navigator (Phase 3) |
| `anupalan.portal.check-completed` | portal-navigator | task-service |

---

## Infrastructure Stack

```mermaid
graph TB
    subgraph "Development Environment"
        DC[docker-compose.yml]
        DC --> PG[PostgreSQL 16<br/>:5432]
        DC --> RP[Redpanda<br/>:9092]
        DC --> RPC[Redpanda Console<br/>:8888]
        DC --> R[Redis 7<br/>:6379]
        DC --> M[MinIO<br/>:9000/:9001]
        DC --> KC[Keycloak 24<br/>:8080]
    end

    style DC fill:#2563eb,color:#fff
```

### Technology Choices

| Layer | Technology | Rationale |
|-------|-----------|-----------|
| Language | Java 21 | LTS, virtual threads, pattern matching |
| Framework | Spring Boot 3.4.3 | Mature ecosystem, production-ready |
| API Gateway | Spring Cloud Gateway | Reactive, Redis rate-limit integration |
| Messaging | Redpanda (Kafka-compatible) | Lightweight, no Zookeeper |
| Database | PostgreSQL 16 | JSONB for extractions, mature tooling |
| Object Storage | MinIO | S3-compatible, self-hosted |
| Cache | Redis 7 | Gateway rate limit, session cache |
| Auth | Keycloak 24 | OAuth2/OIDC, multi-tenant realms |
| AI Framework | LangChain4j | Java-native, type-safe @Tool bindings |
| LLM | Anthropic Claude Haiku | Fast, cost-efficient, function calling |
| Frontend | Next.js 15 + React 19 | Server components, modern DX |
| Styling | Tailwind CSS 3.4 | Utility-first, small bundle |
| Charts | Recharts | React-native, composable |
| Migrations | Flyway | Per-schema versioning |

---

## Deployment Topology

### Development (current)
```
All services on localhost
┌─────────────────────────────────────────┐
│  Docker Compose (infrastructure)        │
│  ├─ PostgreSQL 5432                     │
│  ├─ Redpanda 9092                       │
│  ├─ Redis 6379                          │
│  ├─ MinIO 9000/9001                     │
│  └─ Keycloak 8080                       │
│                                         │
│  Maven (mvn spring-boot:run per svc)    │
│  ├─ :8081 profile-service               │
│  ├─ :8082 anupalan-rule-service         │
│  ├─ :8083 niyamitra-document-vault      │
│  ├─ :8084 niyamitra-task-service        │
│  ├─ :8085 kavach-notification-service   │
│  ├─ :8086 kavach-floor-manager          │
│  └─ :8090 niyamitra-api-gateway         │
│                                         │
│  Next.js (npm run dev)                  │
│  └─ :3000 niyamitra-dashboard           │
└─────────────────────────────────────────┘
```

### Production (target)
```
┌─────────────────────────────────────────┐
│  Kubernetes Cluster                     │
│  ├─ Ingress (NGINX/Istio)               │
│  ├─ API Gateway (3 replicas)            │
│  ├─ Backend services (2-5 replicas)     │
│  ├─ HPA based on CPU/memory/queue depth │
│  └─ Cert-manager + external-dns         │
│                                         │
│  Managed Services                       │
│  ├─ AWS RDS PostgreSQL (Multi-AZ)       │
│  ├─ AWS MSK (Kafka)                     │
│  ├─ AWS ElastiCache (Redis)             │
│  ├─ AWS S3 (object storage)             │
│  └─ Keycloak (EKS or Cloud IAM)         │
└─────────────────────────────────────────┘
```

---

## Cross-References

- [Event Flow Diagrams](./flows.md) — Detailed sequence diagrams for each business flow
- [README](../README.md) — Getting started, local setup, running services
