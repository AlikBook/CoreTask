package Backend.demo.Controllers;

import Backend.demo.Entities.task.Tasks;
import Backend.demo.Entities.task.TaskStatus;
import Backend.demo.Entities.task.TaskCategory;
import Backend.demo.Repositories.task.TasksRepository;
import Backend.demo.Repositories.task.StatusRepository;
import Backend.demo.Repositories.task.TaskCategoryRepository;
import Backend.demo.Repositories.dashboard.TaskHistoryRepository;
import Backend.demo.Entities.dashboard.TaskHistory;
import Backend.demo.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    @Autowired
    private TasksRepository tasksRepository;
    
    @Autowired
    private StatusRepository statusRepository;
    
    @Autowired
    private TaskCategoryRepository categoryRepository;
    
    @Autowired
    private TaskHistoryRepository taskHistoryRepository;
    
    // gRPC client only for cross-database validation (Worker)
    private final WorkerServiceGrpc.WorkerServiceBlockingStub workerGrpcClient;
    private final DashboardServiceGrpc.DashboardServiceBlockingStub dashboardGrpcClient;
    
    public TaskController() {
        // Create gRPC channel for Worker validation
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 9090)
            .usePlaintext()
            .build();
        
        this.workerGrpcClient = WorkerServiceGrpc.newBlockingStub(channel);
        this.dashboardGrpcClient = DashboardServiceGrpc.newBlockingStub(channel);
    }

    @GetMapping
    public List<Tasks> getAllTasks() {
        List<Tasks> taskList = tasksRepository.findAll();
        
        return taskList;
    }

    @GetMapping("/{id}")
    public Tasks getTaskById(@PathVariable Integer id) {
        return tasksRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "GET | Task with id " + id + " not found"));
    }

    @PostMapping
    public Tasks createTask(@RequestBody Tasks task) {
        if (task.getWorkerId() != null && task.getWorkerId() != 0) {
            try {
                WorkerIdRequest workerRequest = WorkerIdRequest.newBuilder()
                    .setId(task.getWorkerId())
                    .build();
                WorkerResponse worker = workerGrpcClient.getWorkerById(workerRequest);
                
                if (worker.getId() > 0) {
                    System.out.println("✓ gRPC: Worker validated - " + worker.getWorkerName() + " " + worker.getWorkerLastName());
                }
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Worker with id " + task.getWorkerId() + " not found via gRPC");
            }
        } else {
            System.out.println("ℹ Task created without assigned worker");
        }
        
        // Validate Status exists using JPA (same database)
        if (task.getStatusId() != null && task.getStatusId() != 0) {
            TaskStatus status = statusRepository.findById(task.getStatusId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Status with id " + task.getStatusId() + " not found"));
            System.out.println("✓ JPA: Status validated - " + status.getStatusName());
        }
        
        // Validate Category exists using JPA (same database)
        if (task.getCategoryId() != null && task.getCategoryId() != 0) {
            TaskCategory category = categoryRepository.findById(task.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Category with id " + task.getCategoryId() + " not found"));
            System.out.println("✓ JPA: Category validated - " + category.getCategoryName());
        }
        
        Tasks savedTask = tasksRepository.save(task);
        
        // Notify dashboard via gRPC
        notifyDashboardTaskChange("CREATE", savedTask);
        
        // Also save to history directly
        String details = "Task created with ID: " + savedTask.getTaskId();
        if (savedTask.getWorkerId() != null) {
            details += ", assigned to worker " + savedTask.getWorkerId();
        }
        TaskHistory history = new TaskHistory("CREATE", savedTask.getTaskName(), details);
        taskHistoryRepository.save(history);
        
        return savedTask;
    }

    @PutMapping("/{id}")
    public Tasks updateTask(@PathVariable Integer id, @RequestBody Tasks updatedTask) {
        return tasksRepository.findById(id)
            .map(existingTask -> {
                if (updatedTask.getWorkerId() != null && updatedTask.getWorkerId() != 0) {
                    try {
                        WorkerIdRequest workerRequest = WorkerIdRequest.newBuilder()
                            .setId(updatedTask.getWorkerId())
                            .build();
                        WorkerResponse worker = workerGrpcClient.getWorkerById(workerRequest);
                        System.out.println("✓ gRPC: Worker validated for update - " + worker.getWorkerName());
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Worker not found via gRPC");
                    }
                }
                
                // Validate Status via JPA if being updated (same database)
                if (updatedTask.getStatusId() != null && updatedTask.getStatusId() != 0) {
                    TaskStatus status = statusRepository.findById(updatedTask.getStatusId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status not found"));
                    existingTask.setStatusId(updatedTask.getStatusId());
                    System.out.println("✓ JPA: Status validated for update - " + status.getStatusName());
                }
                
                // Validate Category via JPA if being updated (same database)
                if (updatedTask.getCategoryId() != null && updatedTask.getCategoryId() != 0) {
                    TaskCategory category = categoryRepository.findById(updatedTask.getCategoryId())
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found"));
                    existingTask.setCategoryId(updatedTask.getCategoryId());
                    System.out.println("✓ JPA: Category validated for update - " + category.getCategoryName());
                }
                
                existingTask.setTaskName(updatedTask.getTaskName());
                existingTask.setDueDate(updatedTask.getDueDate());
                existingTask.setDescription(updatedTask.getDescription());
                existingTask.setWorkerId(updatedTask.getWorkerId());
                
                Tasks saved = tasksRepository.save(existingTask);
                
                // Notify dashboard via gRPC
                notifyDashboardTaskChange("UPDATE", saved);
                
                // Save to history
                String details = "Task updated";
                TaskHistory history = new TaskHistory("UPDATE", saved.getTaskName(), details);
                taskHistoryRepository.save(history);
                
                return saved;
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PUT | Task with id " + id + " not found"));
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Integer id) {
        Tasks task = tasksRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "DELETE | Task with id " + id + " not found"));
        
        String taskName = task.getTaskName();
        
        tasksRepository.deleteById(id);
        
        // Notify dashboard via gRPC
        try {
            TaskChangeRequest request = TaskChangeRequest.newBuilder()
                .setAction("DELETE")
                .setTaskName(taskName)
                .setDetails("Task deleted with ID: " + id)
                .build();
            dashboardGrpcClient.notifyTaskChange(request);
        } catch (Exception e) {
            System.out.println("⚠ Dashboard notification failed: " + e.getMessage());
        }
        
        // Save to history
        TaskHistory history = new TaskHistory("DELETE", taskName, "Task deleted with ID: " + id);
        taskHistoryRepository.save(history);
    }
    
    // Helper method to notify dashboard
    private void notifyDashboardTaskChange(String action, Tasks task) {
        try {
            TaskChangeRequest.Builder requestBuilder = TaskChangeRequest.newBuilder()
                .setAction(action)
                .setTaskName(task.getTaskName());
            
            if (task.getStatusId() != null && task.getStatusId() != 0) {
                requestBuilder.setStatusId(task.getStatusId());
            }
            if (task.getCategoryId() != null && task.getCategoryId() != 0) {
                requestBuilder.setCategoryId(task.getCategoryId());
            }
            
            String details = action + " task: " + task.getTaskName();
            if (task.getWorkerId() != null) {
                details += " (worker: " + task.getWorkerId() + ")";
            }
            requestBuilder.setDetails(details);
            
            dashboardGrpcClient.notifyTaskChange(requestBuilder.build());
        } catch (Exception e) {
            System.out.println("⚠ Dashboard notification failed: " + e.getMessage());
        }
    }
}