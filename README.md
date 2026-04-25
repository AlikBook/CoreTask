# CORE TASK : Task Organizer - Microservices Application

> **🚀 Live Demo:** [core-task.vercel.app](https://core-task.vercel.app/)

The project is a task management system built with microservices architecture, featuring REST APIs, gRPC communication, API Gateway, and containerized deployment. The main objective of the app is to help users to organize their tasks. They can do CRUD operations on them an even assign tasks to workers in the case of an ornaization. Some other features are implemented like task status and categories to imporve management.

## Features

### Architecture & Technologies

- **Microservices Architecture**: Modular backend services with API Gateway pattern
- **RESTful API**: Full CRUD operations for tasks, workers, statuses, and categories
- **gRPC Communication**: Inter-service communication
- **API Gateway**: Spring Cloud Gateway for entry point and routing
- **Database**: H2 in-memory database with JPA/Hibernate
- **Frontend**: Vue.js 3 with Vite
- **Containerization**: Docker and Docker Compose for consistent deployment
- **Cloud-Native**: Multi-stage builds, orchestration support

### Core Functionality

- **Task Management**: Create, update, delete, and organize tasks
- **Worker Assignment**: Manage workers and assign them to tasks
- **Status Tracking**: Custom task statuses with real-time updates
- **Category Organization**: Categorize tasks for better organization
- **Real-time Dashboard**: View statistics and recent activity
- **Activity History**: Track all changes to tasks and workers

## Prerequisites

- **Docker** (version 20.10 or higher)
- **Docker Compose** (version 2.0 or higher)
- **Node.js** (version 20 or higher) - for local frontend development
- **Java 21** - for local backend development (optional)

---

## Installation & Setup

### Option 1: Full Docker Deployment

1. **Clone the repository**
   ```bash
   git clone https://github.com/AlikBook/CoreTask.git
   cd CoreTask
   ```

> **Note:**  
> After cloning the repository, some Java classes (especially gRPC-related) may appear with errors in your IDE.  
> To resolve this, run the following command in the `Backend` and `Gateway` folders:
>
> ```
> ./gradlew build
> ```
>
> This will generate all necessary sources and remove IDE errors.  
> This step is only needed for development visuals; it is not required when running the application with Docker Compose.

2. **Build and start all services**

   ```bash
   docker-compose up --build
   ```

3. **Access the application**
   - Frontend: http://localhost:5173
   - API Gateway: http://localhost:8081
   - Backend API: http://localhost:8080 (via Gateway)

4. **Stop the services**
   ```bash
   docker-compose down
   ```

### Option 2: Hybrid Development Setup

For frontend development with live reload:

1. **Start backend services only**

   ```bash
   docker-compose up backend gateway
   ```

2. **Run frontend locally** (in a separate terminal)

   ```bash
   cd Frontend
   npm install
   npm run dev
   ```

3. **Access the application**
   - Frontend: http://localhost:5173
   - API Gateway: http://localhost:8081

## API Endpoints

### Tasks

- `GET /api/tasks` - Get all tasks
- `GET /api/tasks/{id}` - Get task by ID
- `POST /api/tasks` - Create new task
- `PUT /api/tasks/{id}` - Update task
- `DELETE /api/tasks/{id}` - Delete task

### Workers

- `GET /api/workers` - Get all workers
- `GET /api/workers/{id}` - Get worker by ID
- `POST /api/workers` - Create new worker
- `PUT /api/workers/{id}` - Update worker
- `DELETE /api/workers/{id}` - Delete worker

### Statuses

- `GET /api/task-statuses` - Get all statuses
- `GET /api/task-statuses/{id}` - Get status by ID
- `POST /api/task-statuses` - Create new status
- `PUT /api/task-statuses/{id}` - Update status
- `DELETE /api/task-statuses/{id}` - Delete status

### Categories

- `GET /api/task-categories` - Get all categories
- `GET /api/task-categories/{id}` - Get category by ID
- `POST /api/task-categories` - Create new category
- `PUT /api/task-categories/{id}` - Update category
- `DELETE /api/task-categories/{id}` - Delete category

### Dashboard

- `GET /api/dashboard` - Get dashboard statistics

---

## Configuration

### Backend Configuration

Located in `Backend/src/main/resources/application.properties`:

- Database: H2 in-memory (jdbc:h2:mem:appdb)
- Server port: 8080
- gRPC port: 9090

### Gateway Configuration

Located in `Gateway/src/main/resources/application.properties`:

- Server port: 8081
- Backend route: http://backend:8080 (Docker) or http://localhost:8080 (local)
- CORS: http://localhost:5173

### Frontend Configuration

Located in `Frontend/src/services/api.js`:

- API base URL: http://localhost:8081/api

---

## Docker Configuration

### Backend Dockerfile

- Base image: eclipse-temurin:21-jdk (Debian-based for glibc support)
- Multi-stage build: Build stage + Runtime stage
- Automatic Gradle build inside container

### Gateway Dockerfile

- Base image: eclipse-temurin:21-jdk
- Multi-stage build for optimized image size

### Frontend Dockerfile

- Build stage: Node.js 22
- Runtime stage: nginx:alpine
- Serves static built files

---

## Development

### Building Manually

**Backend:**

```bash
cd Backend
./gradlew clean build --no-daemon
```

**Gateway:**

```bash
cd Gateway
./gradlew clean build --no-daemon
```

**Frontend:**

```bash
cd Frontend
npm install
npm run build
```

### Running Tests

**Backend:**

```bash
cd Backend
./gradlew test
```

---

## gRPC Services

The application uses gRPC for inter-service communication:

- **WorkerService**: Worker entity validation and retrieval
- **TaskService**: Task operations and updates
- **StatusService**: Status validation
- **CategoryService**: Category validation
- **DashboardService**: Real-time dashboard updates and notifications

Protocol Buffer definitions are located in `Backend/src/main/proto/`.

---

## Database Schema

The application uses a single H2 in-memory database with the following entities:

- **Tasks**: Main task entity with references to workers, statuses, and categories
- **Workers**: User information and worker details
- **TaskStatus**: Available task statuses
- **TaskCategory**: Task categories
- **TaskHistory**: Activity log for dashboard

**Note**: The application uses integer IDs instead of JPA foreign keys, with validation handled via gRPC calls.

### Database Access

H2 Console: http://localhost:8080/h2-console

- JDBC URL: `jdbc:h2:mem:appdb`
- User: `sa`
- Password: (empty)

---

## Ports

- **Frontend**: 5173 (development) / 80 (container)
- **API Gateway**: 8081
- **Backend REST**: 8080
- **Backend gRPC**: 9090

---

## How gRPC Validation Works

When creating or updating a task:

1. **REST Controller** receives the request
2. **gRPC Client** validates referenced entities:
   - Calls `WorkerService.getWorkerById()` for worker validation
   - Calls `StatusService.getStatusById()` for status validation
   - Calls `CategoryService.getCategoryById()` for category validation
3. **Validation Result**:
   - All entities exist → Task is saved
   - Entity not found → HTTP 400 error with descriptive message
4. **Dashboard Notification**: On success, `DashboardService.notifyTaskChange()` is called via gRPC
