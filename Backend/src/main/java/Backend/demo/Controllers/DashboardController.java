package Backend.demo.Controllers;

import Backend.demo.Entities.dashboard.TaskHistory;
import Backend.demo.Entities.task.TaskCategory;
import Backend.demo.Entities.task.TaskStatus;
import Backend.demo.Entities.task.Tasks;
import Backend.demo.Repositories.dashboard.TaskHistoryRepository;
import Backend.demo.Repositories.task.TaskCategoryRepository;
import Backend.demo.Repositories.task.StatusRepository;
import Backend.demo.Repositories.task.TasksRepository;
import Backend.demo.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    // NO direct access to WorkerRepository - use gRPC instead!
    private final WorkerServiceGrpc.WorkerServiceBlockingStub workerGrpcClient;
    
    @Autowired
    private TasksRepository tasksRepository;
    
    @Autowired
    private StatusRepository statusRepository;
    
    @Autowired
    private TaskCategoryRepository categoryRepository;
    
    @Autowired
    private TaskHistoryRepository taskHistoryRepository;
    
    public DashboardController() {
        // Create gRPC channel for Worker service (cross-database)
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 9090)
            .usePlaintext()
            .build();
        this.workerGrpcClient = WorkerServiceGrpc.newBlockingStub(channel);
    }

    @GetMapping
    public Map<String, Object> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Count workers via gRPC (cross-database) ✨
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            WorkerListResponse workers = workerGrpcClient.getAllWorkers(request);
            dashboard.put("totalWorkers", workers.getWorkersCount());
            System.out.println("✓ gRPC: Retrieved worker count: " + workers.getWorkersCount());
        } catch (Exception e) {
            System.out.println("⚠ gRPC: Failed to get worker count: " + e.getMessage());
            dashboard.put("totalWorkers", 0);
        }
        
        // Count tasks
        long totalTasks = tasksRepository.count();
        dashboard.put("totalTasks", totalTasks);
        
        // Tasks by status
        Map<String, Long> tasksByStatus = new HashMap<>();
        List<TaskStatus> allStatuses = statusRepository.findAll();
        List<Tasks> allTasks = tasksRepository.findAll();
        
        for (TaskStatus status : allStatuses) {
            long count = allTasks.stream()
                .filter(task -> task.getStatus() != null && task.getStatus().getStatusId().equals(status.getStatusId()))
                .count();
            tasksByStatus.put(status.getStatusName(), count);
        }
        dashboard.put("tasksByStatus", tasksByStatus);
        
        // Tasks by category
        Map<String, Long> tasksByCategory = new HashMap<>();
        List<TaskCategory> allCategories = categoryRepository.findAll();
        
        for (TaskCategory category : allCategories) {
            long count = allTasks.stream()
                .filter(task -> task.getCategory() != null && task.getCategory().getCategoryId().equals(category.getCategoryId()))
                .count();
            tasksByCategory.put(category.getCategoryName(), count);
        }
        dashboard.put("tasksByCategory", tasksByCategory);
        
        // Get recent history
        List<TaskHistory> recentHistory = taskHistoryRepository.findTop20ByOrderByTimestampDesc();
        dashboard.put("recentHistory", recentHistory);
        
        return dashboard;
    }
    
    @GetMapping("/history")
    public List<TaskHistory> getHistory() {
        return taskHistoryRepository.findTop20ByOrderByTimestampDesc();
    }
}
