package Backend.demo.grpc;

import Backend.demo.Entities.dashboard.TaskHistory;
import Backend.demo.Entities.task.TaskCategory;
import Backend.demo.Entities.task.TaskStatus;
import Backend.demo.Entities.task.Tasks;
import Backend.demo.Repositories.dashboard.TaskHistoryRepository;
import Backend.demo.Repositories.task.TaskCategoryRepository;
import Backend.demo.Repositories.task.StatusRepository;
import Backend.demo.Repositories.task.TasksRepository;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@GrpcService
public class DashboardServiceImpl extends DashboardServiceGrpc.DashboardServiceImplBase {

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
    
    public DashboardServiceImpl() {
        // Create gRPC channel for Worker service (cross-database)
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 9090)
            .usePlaintext()
            .build();
        this.workerGrpcClient = WorkerServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public void getDashboard(EmptyRequest request, StreamObserver<DashboardResponse> responseObserver) {
        // Count workers via gRPC (cross-database) ✨
        int totalWorkers = 0;
        try {
            EmptyRequest workerRequest = EmptyRequest.newBuilder().build();
            WorkerListResponse workers = workerGrpcClient.getAllWorkers(workerRequest);
            totalWorkers = workers.getWorkersCount();
            System.out.println("✓ gRPC: Dashboard retrieved worker count: " + totalWorkers);
        } catch (Exception e) {
            System.out.println("⚠ gRPC: Failed to get worker count for dashboard: " + e.getMessage());
        }
        
        // Count tasks
        long totalTasks = tasksRepository.count();
        List<Tasks> allTasks = tasksRepository.findAll();
        
        // Tasks by status
        Map<String, Integer> tasksByStatus = new HashMap<>();
        List<TaskStatus> allStatuses = statusRepository.findAll();
        for (TaskStatus status : allStatuses) {
            long count = allTasks.stream()
                .filter(task -> task.getStatus() != null && task.getStatus().getStatusId().equals(status.getStatusId()))
                .count();
            tasksByStatus.put(status.getStatusName(), (int) count);
        }
        
        // Tasks by category
        Map<String, Integer> tasksByCategory = new HashMap<>();
        List<TaskCategory> allCategories = categoryRepository.findAll();
        for (TaskCategory category : allCategories) {
            long count = allTasks.stream()
                .filter(task -> task.getCategory() != null && task.getCategory().getCategoryId().equals(category.getCategoryId()))
                .count();
            tasksByCategory.put(category.getCategoryName(), (int) count);
        }
        
        // Get recent history
        List<TaskHistory> recentHistory = taskHistoryRepository.findTop20ByOrderByTimestampDesc();
        
        DashboardResponse.Builder responseBuilder = DashboardResponse.newBuilder()
            .setTotalWorkers(totalWorkers)
            .setTotalTasks((int) totalTasks)
            .putAllTasksByStatus(tasksByStatus)
            .putAllTasksByCategory(tasksByCategory);
        
        for (TaskHistory history : recentHistory) {
            TaskHistoryResponse historyResponse = TaskHistoryResponse.newBuilder()
                .setId(history.getHistoryId())
                .setAction(history.getAction())
                .setTaskName(history.getTaskName())
                .setTimestamp(history.getTimestamp().toString())
                .setDetails(history.getDetails() != null ? history.getDetails() : "")
                .build();
            responseBuilder.addRecentHistory(historyResponse);
        }
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void notifyTaskChange(TaskChangeRequest request, StreamObserver<EmptyResponse> responseObserver) {
        // Create history entry
        String details = request.getDetails();
        if (details == null || details.isEmpty()) {
            details = request.getAction() + " task: " + request.getTaskName();
        }
        
        TaskHistory history = new TaskHistory(request.getAction(), request.getTaskName(), details);
        taskHistoryRepository.save(history);
        
        System.out.println("📊 Dashboard: Task " + request.getAction() + " - " + request.getTaskName());
        
        responseObserver.onNext(EmptyResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void notifyWorkerChange(WorkerChangeRequest request, StreamObserver<EmptyResponse> responseObserver) {
        String details = request.getDetails();
        if (details == null || details.isEmpty()) {
            details = request.getAction() + " worker: " + request.getWorkerName();
        }
        
        TaskHistory history = new TaskHistory(request.getAction() + "_WORKER", request.getWorkerName(), details);
        taskHistoryRepository.save(history);
        
        System.out.println("📊 Dashboard: Worker " + request.getAction() + " - " + request.getWorkerName());
        
        responseObserver.onNext(EmptyResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
