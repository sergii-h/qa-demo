# Quick Reference - Task Management Service Requirements

## 📁 Structure Overview
```
requirements/
├── BACKLOG.md                              # Complete backlog summary
├── README.md                               # Navigation guide
├── epic-1-task-management/                 # 5 stories, 17 pts
│   ├── TASK-001-create-task.md
│   ├── TASK-002-retrieve-task.md
│   ├── TASK-003-update-task.md
│   ├── TASK-004-delete-task.md
│   └── TASK-005-list-tasks.md
├── epic-2-task-validation/                 # 1 story, 3 pts
│   └── VALID-001-external-validation.md
├── epic-3-event-streaming/                 # 3 stories, 9 pts
│   ├── EVENT-001-task-created-event.md
│   ├── EVENT-002-task-updated-event.md
│   └── EVENT-003-task-deleted-event.md
└── epic-4-system-configuration/            # 3 stories, 8 pts
    ├── CONFIG-001-cors-configuration.md
    ├── CONFIG-002-error-handling.md
    └── CONFIG-003-external-integrations.md
```

## 📊 Quick Stats
- **Total Epics**: 4
- **Total Stories**: 12
- **Total Story Points**: 37 points

## 🎯 Epic Breakdown

| Epic | Focus | Stories | Points |
|------|-------|---------|--------|
| **1. Task Management** | CRUD operations | 5 | 17 |
| **2. Task Validation** | External validation | 1 | 3 |
| **3. Event Streaming** | Kafka events | 3 | 9 |
| **4. System Config** | Infrastructure | 3 | 8 |

## 🔗 API Endpoints

### Task CRUD
```
POST   /v1/tasks          → 201, 400, 409
GET    /v1/tasks/{id}     → 200, 404
PUT    /v1/tasks/{id}     → 200, 400, 404, 409
DELETE /v1/tasks/{id}     → 204, 404
GET    /v1/tasks          → 200
```

### Task Validation
```
GET /v1/tasks/isValid/{id} → 200
```

## 💾 Data Model Quick Ref

**Task Fields:**
- `id` - MongoDB ObjectId (auto)
- `title` - Required, max 100 chars, unique
- `description` - Optional, max 500 chars
- `status` - TODO | IN_PROGRESS | DONE
- `priority` - LOW | MEDIUM | HIGH
- `createdDate` - Auto timestamp
- `updatedDate` - Auto timestamp

**Task Event:**
- `taskId`, `title`, `status`, `priority`, `timestamp`, `eventType`

## ⚙️ Configuration

```properties
spring.data.mongodb.uri=mongodb://localhost:27018/task_db
kafka.bootstrap-servers=localhost:9094
kafka.topic.task-event=task-event
external.service.url=http://localhost:8085
```

## 📋 Story Format

Each story includes:
- **Epic** assignment
- **Priority** (High/Medium/Low)
- **Story Points** estimate
- **Description** (user story format)
- **Acceptance Criteria** (Given-When-Then)
- **Technical Notes**

## 🔍 Finding Stories

- **By Epic**: Browse epic folders (epic-1 through epic-4)
- **By Feature**: Check epic README files
- **Complete List**: See BACKLOG.md
- **Navigation**: Start with requirements/README.md

## 📝 Ticket Prefixes

- `TASK-` → Task Management features
- `VALID-` → Validation features  
- `EVENT-` → Event streaming features
- `CONFIG-` → System configuration features

---

**Start Here**: [`requirements/BACKLOG.md`](BACKLOG.md) for complete backlog  
**Or**: [`requirements/README.md`](README.md) for navigation guide

