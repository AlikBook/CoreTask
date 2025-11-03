package Backend.demo.grpc;

import Backend.demo.Entities.task.Tasks;
import Backend.demo.Repositories.task.TasksRepository;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@GrpcService
public class TaskServiceImpl extends TaskServiceGrpc.TaskServiceImplBase {

    @Autowired
    private TasksRepository tasksRepository;

    @Override
    public void getAllTasks(EmptyRequest request, StreamObserver<TaskListResponse> responseObserver) {
        List<Tasks> tasks = tasksRepository.findAll();
        
        TaskListResponse.Builder responseBuilder = TaskListResponse.newBuilder();
        
        for (Tasks task : tasks) {
            TaskResponse taskResponse = TaskResponse.newBuilder()
                .setId(task.getTaskId())
                .setTaskName(task.getTaskName())
                .setDescription(task.getDescription() != null ? task.getDescription() : "")
                .setWorkerId(task.getWorkerId() != null ? task.getWorkerId() : 0)
                .setStatusId(task.getStatus() != null ? task.getStatus().getStatusId() : 0)
                .setCategoryId(task.getCategory() != null ? task.getCategory().getCategoryId() : 0)
                .build();
            responseBuilder.addTasks(taskResponse);
        }
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void getTaskById(TaskIdRequest request, StreamObserver<TaskResponse> responseObserver) {
        Tasks task = tasksRepository.findById(request.getId()).orElse(null);
        
        TaskResponse.Builder responseBuilder = TaskResponse.newBuilder();
        
        if (task != null) {
            responseBuilder
                .setId(task.getTaskId())
                .setTaskName(task.getTaskName())
                .setDescription(task.getDescription() != null ? task.getDescription() : "")
                .setWorkerId(task.getWorkerId() != null ? task.getWorkerId() : 0)
                .setStatusId(task.getStatus() != null ? task.getStatus().getStatusId() : 0)
                .setCategoryId(task.getCategory() != null ? task.getCategory().getCategoryId() : 0);
        }
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void unassignWorkerFromTasks(UnassignWorkerRequest request, StreamObserver<UnassignWorkerResponse> responseObserver) {
        int workerId = request.getWorkerId();
        
        // Find all tasks assigned to this worker
        List<Tasks> tasks = tasksRepository.findAll();
        int modifiedCount = 0;
        
        for (Tasks task : tasks) {
            if (task.getWorkerId() != null && task.getWorkerId().equals(workerId)) {
                task.setWorkerId(null);
                tasksRepository.save(task);
                modifiedCount++;
                System.out.println("✓ gRPC: Unassigned worker " + workerId + " from task '" + task.getTaskName() + "'");
            }
        }
        
        UnassignWorkerResponse response = UnassignWorkerResponse.newBuilder()
            .setTasksModified(modifiedCount)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createTask(CreateTaskRequest request, StreamObserver<TaskResponse> responseObserver) {
        // This can be implemented if needed for full gRPC support
        responseObserver.onNext(TaskResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateTask(UpdateTaskRequest request, StreamObserver<TaskResponse> responseObserver) {
        // This can be implemented if needed for full gRPC support
        responseObserver.onNext(TaskResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteTask(TaskIdRequest request, StreamObserver<EmptyResponse> responseObserver) {
        // This can be implemented if needed for full gRPC support
        responseObserver.onNext(EmptyResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
