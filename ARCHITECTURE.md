# Architecture Diagram

## High-Level Architecture
```
┌─────────────────────────────────────────┐
│           HTTP Client                    │
│     (Postman / Mobile App / Web)        │
└─────────────────┬───────────────────────┘
                  │ HTTP Request (JSON)
                  ▼
┌─────────────────────────────────────────┐
│        TransactionController            │
│  - @RestController                      │
│  - HTTP validation                      │
│  - Response formatting                  │
└─────────────────┬───────────────────────┘
                  │ Method call
                  ▼
┌─────────────────────────────────────────┐
│        TransactionService               │
│  - @Service                             │
│  - Business validation                  │
│  - ID generation                        │
│  - Transaction orchestration            │
└─────────────────┬───────────────────────┘
                  │ Repository call
                  ▼
┌─────────────────────────────────────────┐
│       TransactionRepository             │
│  - JpaRepository interface              │
│  - Custom query methods                 │
│  - CRUD operations                      │
└─────────────────┬───────────────────────┘
                  │ SQL via Hibernate
                  ▼
┌─────────────────────────────────────────┐
│            Hibernate (ORM)              │
│  - Entity mapping                       │
│  - SQL generation                       │
│  - Transaction management               │
└─────────────────┬───────────────────────┘
                  │ JDBC
                  ▼
┌─────────────────────────────────────────┐
│         PostgreSQL Database             │
│  - transactions table                   │
│  - 5 indexes                            │
│  - ACID compliance                      │
└─────────────────────────────────────────┘
```

## Data Flow Example

**Create Transaction Request:**
```
1. Client: POST /api/transactions {"fromAccount":"001", ...}
2. Controller: Receives HTTP, validates format, calls service
3. Service: Validates business rules (same account check)
4. Service: Generates ID (TX0001)
5. Repository: save(transaction)
6. Hibernate: INSERT INTO transactions ...
7. PostgreSQL: Persists data, returns success
8. Hibernate: Maps result to Transaction object
9. Repository: Returns Transaction
10. Service: Returns Transaction
11. Controller: Builds JSON response, returns 201 Created
12. Client: Receives {"transactionId":"TX0001", ...}
```

## Component Responsibilities

| Layer | Responsibility | Does NOT Do |
|-------|---------------|-------------|
| **Controller** | HTTP handling, input format validation, response formatting | Business logic, database calls |
| **Service** | Business validation, transaction orchestration, ID generation | HTTP handling, SQL queries |
| **Repository** | Database queries, CRUD operations | Business logic, HTTP |
| **Hibernate** | ORM, SQL generation, entity mapping | Business rules, validation |
| **Database** | Data persistence, ACID transactions | Business logic, validation |

## Key Patterns

**Repository Pattern:** Abstracts data access  
**Service Pattern:** Centralizes business logic  
**DTO Pattern:** Transaction entity serves as both domain and DTO  
**Exception Handling:** GlobalExceptionHandler for consistent errors
```