package Backend.demo.Controllers;

import Backend.demo.Entities.task.Tasks;
import Backend.demo.Repositories.task.TasksRepository;
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
    
    
    private final WorkerServiceGrpc.WorkerServiceBlockingStub workerGrpcClient;
    private final DashboardServiceGrpc.DashboardServiceBlockingStub dashboardGrpcClient;
    private final StatusServiceGrpc.StatusServiceBlockingStub statusGrpcClient;
    private final CategoryServiceGrpc.CategoryServiceBlockingStub categoryGrpcClient;
    
    public TaskController() {
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 9090)
            .usePlaintext()
            .build();
        
        this.workerGrpcClient = WorkerServiceGrpc.newBlockingStub(channel);
        this.dashboardGrpcClient = DashboardServiceGrpc.newBlockingStub(channel);
        this.statusGrpcClient = StatusServiceGrpc.newBlockingStub(channel);
        this.categoryGrpcClient = CategoryServiceGrpc.newBlockingStub(channel);
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
                    System.out.println("gRPC: Worker validated - " + worker.getWorkerName() + " " + worker.getWorkerLastName());
                }
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Worker with id " + task.getWorkerId() + " not found via gRPC");
            }
        } else {
            System.out.println("Task created without assigned worker");
        }
        
        if (task.getStatusId() != null && task.getStatusId() != 0) {
            try {
                StatusIdRequest statusRequest = StatusIdRequest.newBuilder()
                    .setId(task.getStatusId())
                    .build();
                StatusResponse status = statusGrpcClient.getStatusById(statusRequest);
                System.out.println("gRPC: Status validated - " + status.getStatusName());
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Status with id " + task.getStatusId() + " not found via gRPC");
            }
        }
        
        if (task.getCategoryId() != null && task.getCategoryId() != 0) {
            try {
                CategoryIdRequest categoryRequest = CategoryIdRequest.newBuilder()
                    .setId(task.getCategoryId())
                    .build();
                CategoryResponse category = categoryGrpcClient.getCategoryById(categoryRequest);
                System.out.println("gRPC: Category validated - " + category.getCategoryName());
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Category with id " + task.getCategoryId() + " not found via gRPC");
            }
        }
        
        Tasks savedTask = tasksRepository.save(task);
        
        notifyDashboardTaskChange("CREATE", savedTask);
        
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
                        System.out.println("gRPC: Worker validated for update - " + worker.getWorkerName());
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Worker not found via gRPC");
                    }
                }
                
                if (updatedTask.getStatusId() != null && updatedTask.getStatusId() != 0) {
                    try {
                        StatusIdRequest statusRequest = StatusIdRequest.newBuilder()
                            .setId(updatedTask.getStatusId())
                            .build();
                        StatusResponse status = statusGrpcClient.getStatusById(statusRequest);
                        existingTask.setStatusId(updatedTask.getStatusId());
                        System.out.println("gRPC: Status validated for update - " + status.getStatusName());
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status not found via gRPC");
                    }
                }
                
                if (updatedTask.getCategoryId() != null && updatedTask.getCategoryId() != 0) {
                    try {
                        CategoryIdRequest categoryRequest = CategoryIdRequest.newBuilder()
                            .setId(updatedTask.getCategoryId())
                            .build();
                        CategoryResponse category = categoryGrpcClient.getCategoryById(categoryRequest);
                        existingTask.setCategoryId(updatedTask.getCategoryId());
                        System.out.println("gRPC: Category validated for update - " + category.getCategoryName());
                    } catch (Exception e) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found via gRPC");
                    }
                }
                
                existingTask.setTaskName(updatedTask.getTaskName());
                existingTask.setDueDate(updatedTask.getDueDate());
                existingTask.setDescription(updatedTask.getDescription());
                existingTask.setWorkerId(updatedTask.getWorkerId());
                
                Tasks saved = tasksRepository.save(existingTask);
                
                notifyDashboardTaskChange("UPDATE", saved);
                
                
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
        
        try {
            TaskChangeRequest request = TaskChangeRequest.newBuilder()
                .setAction("DELETE")
                .setTaskName(taskName)
                .setDetails("Task deleted with ID: " + id)
                .build();
            dashboardGrpcClient.notifyTaskChange(request);
        } catch (Exception e) {
            System.out.println("Dashboard notification failed: " + e.getMessage());
        }
        
        
    }
    
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
            System.out.println("Dashboard notification failed: " + e.getMessage());
        }
    }
}