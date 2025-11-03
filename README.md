# REST-GRPC Project - Dual Database Architecture

A Spring Boot application demonstrating REST API and gRPC communication with a microservice-inspired dual-database architecture.

## 🎯 Project Overview

This project implements a **task management system** with **two separate databases** to simulate a realistic microservice architecture where:
- **Task Database (taskdb)**: Manages tasks, task statuses, and task categories
- **Worker Database (workerdb)**: Manages workers and user information

The separation of databases makes **gRPC communication essential** for validating cross-database references, demonstrating a real-world use case for RPC services.

## 🏗️ Architecture

```
┌──────────────┐
│   Client     │
└──────┬───────┘
       │ REST API (Port 8080)
       │
┌──────▼──────────────────────────┐
│   Spring Boot Application       │
│                                  │
│  ┌────────────────────────────┐ │
│  │   REST Controllers         │ │
│  └──────┬──────────────┬──────┘ │
│         │              │         │
│    JPA/Hibernate    gRPC Client │
│         │              │         │
│  ┌──────▼──────┐  ┌───▼──────┐ │
│  │   taskdb    │  │ workerdb │ │
│  │ Repositories│  │ Repos    │ │
│  └─────────────┘  └──────────┘ │
│                                  │
│  ┌────────────────────────────┐ │
│  │   gRPC Services            │ │
│  │   (Port 9090)              │ │
│  └────────────────────────────┘ │
└──────────────────────────────────┘
```

## 🔑 Key Features

### Dual Database System
- **Task Database**: Tasks, TaskStatus, TaskCategory
- **Worker Database**: Worker, UserInfo
- Separate HikariCP connection pools
- Independent EntityManagerFactories

### REST API
- Full CRUD operations for all entities
- JSON request/response format
- HTTP/1.1 protocol
- Port: 8080

### gRPC Services
- Cross-database validation
- Binary Protocol Buffers format
- HTTP/2 protocol
- Port: 9090
- Services:
  - TaskService
  - WorkerService
  - TaskStatusService
  - TaskCategoryService

## 🚀 Quick Start

### Prerequisites
- Java 21
- Gradle (included via wrapper)

### Build the Project
```bash
cd Backend
./gradlew clean build
```

### Run the Application
```bash
./gradlew bootRun
```

### Verify Startup
Look for these log messages:
```
✓ Initialized JPA EntityManagerFactory for persistence unit 'task'
✓ Initialized JPA EntityManagerFactory for persistence unit 'worker'
✓ Registered gRPC service: TaskService
✓ Registered gRPC service: WorkerService
✓ gRPC Server started, listening on address: *, port: 9090
✓ Tomcat started on port 8080 (http)
```

## 📚 Documentation

- **[MIGRATION_SUMMARY.md](MIGRATION_SUMMARY.md)** - Complete migration documentation
- **[ARCHITECTURE_DIAGRAM.md](ARCHITECTURE_DIAGRAM.md)** - Detailed architecture diagrams
- **[TESTING_GUIDE.md](TESTING_GUIDE.md)** - Comprehensive testing guide
- **[Backend/api-tests-2db.http](Backend/api-tests-2db.http)** - REST API test requests

## 🧪 Testing

### Using VS Code REST Client
Open `Backend/api-tests-2db.http` and run the requests in sequence:
1. Create statuses and categories
2. Create workers
3. Create tasks (with automatic gRPC validation)
4. Test validation errors

### Expected Console Output
When creating a task with valid references:
```
✓ gRPC: Worker validated - John Doe
✓ gRPC: Status validated - In Progress
✓ gRPC: Category validated - Development
```

### H2 Console
Access the database consoles at: http://localhost:8080/h2-console

**Task Database:**
- JDBC URL: `jdbc:h2:mem:taskdb`
- User: `sa`
- Password: (empty)

**Worker Database:**
- JDBC URL: `jdbc:h2:mem:workerdb`
- User: `sa`
- Password: (empty)

## 💡 How gRPC Validation Works

When creating a task with `workerId: 1`:

1. **REST Controller** receives the request
2. **gRPC Client** calls `WorkerService.getWorkerById(1)`
3. **gRPC Server** queries the workerdb database
4. **Validation Result**:
   - ✅ Worker exists → Task is saved
   - ❌ Worker not found → HTTP 400 error

This demonstrates **cross-database communication** that would be necessary in a real microservice architecture.

## 📦 Project Structure

```
Backend/
├── src/main/
│   ├── java/Backend/demo/
│   │   ├── config/              # Database configurations
│   │   ├── Controllers/         # REST API endpoints
│   │   ├── Entities/
│   │   │   ├── task/           # Task DB entities
│   │   │   └── worker/         # Worker DB entities
│   │   ├── Repositories/
│   │   │   ├── task/           # Task DB repositories
│   │   │   └── worker/         # Worker DB repositories
│   │   └── grpc/               # gRPC service implementations
│   ├── proto/                  # Protocol Buffer definitions
│   └── resources/
│       └── application.properties
├── build.gradle.kts            # Gradle build configuration
└── api-tests-2db.http         # REST API test file
```

## 🔧 Technology Stack

- **Spring Boot** 3.5.7
- **Java** 21
- **Gradle** (Kotlin DSL)
- **H2 Database** (In-memory)
- **JPA/Hibernate** 6.6.33
- **gRPC** 2.15.0
- **Protocol Buffers** 3.25.3
- **HikariCP** (Connection pooling)

## 🎓 Educational Value

This project demonstrates:
1. ✅ **REST API** with full CRUD operations
2. ✅ **gRPC/RPC** service implementation
3. ✅ **Database access** with JPA
4. ✅ **Multi-database configuration** in Spring Boot
5. ✅ **Microservice communication patterns**
6. ✅ **Protocol Buffers** for type-safe APIs
7. ✅ **Cross-database validation** without foreign keys

## 📝 API Examples

### Create a Worker (workerdb)
```http
POST http://localhost:8080/workers
Content-Type: application/json

{
  "workerName": "John",
  "workerLastName": "Doe"
}
```

### Create a Task (with gRPC validation)
```http
POST http://localhost:8080/tasks
Content-Type: application/json

{
  "taskName": "Complete Project",
  "description": "Finish the OOSD project",
  "workerId": 1,      # ← Validated via gRPC
  "statusId": 1,      # ← Validated via gRPC
  "categoryId": 1     # ← Validated via gRPC
}
```

## 🔍 Why Two Databases?

In a real microservice architecture:
- Different teams may own different databases
- Services need to scale independently
- Database technology may differ per service
- Cross-service calls use APIs (REST/gRPC), not SQL joins

This project simulates that scenario, making gRPC validation **necessary and realistic** rather than redundant.

## 🤝 Contributing

This is an academic project for the OOSD course at EFREI Paris.

## 📄 License

This project is created for educational purposes.
