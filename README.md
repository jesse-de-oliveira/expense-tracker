# Expense Tracker API

Production-grade REST API for financial transactions. Built with Spring Boot, PostgreSQL, and clean 3-tier architecture.

## Features

- Transaction CRUD operations
- Filter by account, status, amount
- Real-time statistics
- PostgreSQL persistence
- Database indexes for performance
- Professional error handling

## Tech Stack

**Backend:** Java 21 (LTS), Spring Boot 4.0.2
**Database:** PostgreSQL 18.2 (Relational)
**ORM/JPA:** Hibernate 7.2.1 / Spring Data JPA
**Connection Pool:** HikariCP
**Build Tool:** Maven

## Quick Start

### 1. Setup Database
```sql
psql -U postgres
CREATE DATABASE expense_tracker_db;
\q
```

### 2. Configure
Edit `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/expense_tracker_db
spring.datasource.username=postgres
spring.datasource.password=your_password
```

### 3. Run
```bash
mvn spring-boot:run
```

App runs on `http://localhost:8080`

## API Endpoints

**Create Transaction:**
```http
POST /api/transactions
Content-Type: application/json

{
  "fromAccount": "001",
  "toAccount": "002",
  "amount": 500.00,
  "description": "Rent payment"
}
```

**Get All:**
```http
GET /api/transactions
GET /api/transactions?account=001
GET /api/transactions?status=completed
```

**Get by ID:**
```http
GET /api/transactions/TX0001
```

**Update:**
```http
PUT /api/transactions/TX0001?status=refunded
```

**Delete:**
```http
DELETE /api/transactions/TX0001
```

**Statistics:**
```http
GET /api/transactions/stats
```

## Architecture
```
HTTP Request
    ↓
Controller (HTTP handling)
    ↓
Service (Business logic)
    ↓
Repository (Data access)
    ↓
PostgreSQL Database
```

**3-Tier Design:**
- **Controller**: HTTP requests/responses only
- **Service**: Business validation & orchestration
- **Repository**: Database queries via JpaRepository

## Database Schema

**Transactions Table:**
```sql
transaction_id   VARCHAR(50)   PRIMARY KEY
from_account     VARCHAR(20)   NOT NULL, INDEXED
to_account       VARCHAR(20)   NOT NULL, INDEXED
amount           NUMERIC(19,2) NOT NULL
currency         VARCHAR(3)
status           VARCHAR(20)   INDEXED
description      VARCHAR(200)
timestamp        TIMESTAMP     NOT NULL, INDEXED
```

**Indexes:**
- `idx_from_account` - Fast account queries
- `idx_to_account` - Fast recipient queries
- `idx_status` - Fast status filtering
- `idx_timestamp` - Fast date sorting
- `idx_account_status` - Composite filter optimization

## Example Usage
```bash
# Create transaction
curl -X POST http://localhost:8080/api/transactions \
  -H "Content-Type: application/json" \
  -d '{"fromAccount":"001","toAccount":"002","amount":500}'

# Get all
curl http://localhost:8080/api/transactions

# Filter by account
curl "http://localhost:8080/api/transactions?account=001"

# Get statistics
curl http://localhost:8080/api/transactions/stats
```

## Key Features

**Validation:**
- Amount must be positive
- Max R50,000 per transaction
- Cannot transfer to same account
- Account ID: 3-20 characters

**Error Handling:**
- 400 Bad Request - Validation failed
- 404 Not Found - Transaction doesn't exist
- 409 Conflict - Duplicate transaction
- 500 Internal Server Error - Server issue

**Business Rules:**
- Auto-generate transaction IDs (TX0001, TX0002...)
- Default currency: ZAR
- Refunded transactions cannot be modified
- Completed transactions cannot be deleted

## 📦 Project Structure
```
src/main/java/com/fintech/expensetracker/
├── ExpenseTrackerApplication.java
├── Transaction.java (Entity)
├── TransactionRepository.java (Data layer)
├── TransactionService.java (Business layer)
├── TransactionController.java (HTTP layer)
└── exceptions/
    ├── ResourceNotFoundException.java
    ├── DuplicateResourceException.java
    ├── InvalidOperationException.java
    └── GlobalExceptionHandler.java
```

## 📝 License

MIT License

## 👤 Author

Jesse De Oliveira - Software Engineering Student  
Cape Town, South Africa

**Contact:** [jessedeoliveira15@gmail.com]

---

Built as part of 6-month portfolio plan targeting fintech understanding and preparation.