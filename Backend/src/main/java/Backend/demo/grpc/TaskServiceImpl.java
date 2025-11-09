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
                .setStatusId(task.getStatusId() != null ? task.getStatusId() : 0)
                .setCategoryId(task.getCategoryId() != null ? task.getCategoryId() : 0)
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
                .setStatusId(task.getStatusId() != null ? task.getStatusId() : 0)
                .setCategoryId(task.getCategoryId() != null ? task.getCategoryId() : 0);
        }
        
        responseObserver.onNext(responseBuilder.build());
        responseObserver.onCompleted();
    }

    @Override
    public void unassignWorkerFromTasks(UnassignWorkerRequest request, StreamObserver<UnassignWorkerResponse> responseObserver) {
        int workerId = request.getWorkerId();
        
        List<Tasks> tasks = tasksRepository.findAll();
        int modifiedCount = 0;
        
        for (Tasks task : tasks) {
            if (task.getWorkerId() != null && task.getWorkerId().equals(workerId)) {
                task.setWorkerId(null);
                tasksRepository.save(task);
                modifiedCount++;
                System.out.println("gRPC: Unassigned worker " + workerId + " from task '" + task.getTaskName() + "'");
            }
        }
        
        UnassignWorkerResponse response = UnassignWorkerResponse.newBuilder()
            .setTasksModified(modifiedCount)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void reassignTasksWithStatus(ReassignStatusRequest request, StreamObserver<ReassignStatusResponse> responseObserver) {
        int oldStatusId = request.getOldStatusId();
        int newStatusId = request.getNewStatusId();
        
        List<Tasks> tasks = tasksRepository.findAll();
        int modifiedCount = 0;
        
        for (Tasks task : tasks) {
            if (task.getStatusId() != null && task.getStatusId().equals(oldStatusId)) {
                task.setStatusId(newStatusId == 0 ? null : newStatusId);
                tasksRepository.save(task);
                modifiedCount++;
                String action = newStatusId == 0 ? "unassigned from" : "reassigned from " + oldStatusId + " to";
                System.out.println("gRPC: Task '" + task.getTaskName() + "' " + action + " status " + (newStatusId == 0 ? oldStatusId : newStatusId));
            }
        }
        
        ReassignStatusResponse response = ReassignStatusResponse.newBuilder()
            .setTasksModified(modifiedCount)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void reassignTasksWithCategory(ReassignCategoryRequest request, StreamObserver<ReassignCategoryResponse> responseObserver) {
        int oldCategoryId = request.getOldCategoryId();
        int newCategoryId = request.getNewCategoryId();
        
        List<Tasks> tasks = tasksRepository.findAll();
        int modifiedCount = 0;
        
        for (Tasks task : tasks) {
            if (task.getCategoryId() != null && task.getCategoryId().equals(oldCategoryId)) {
                task.setCategoryId(newCategoryId == 0 ? null : newCategoryId);
                tasksRepository.save(task);
                modifiedCount++;
                String action = newCategoryId == 0 ? "unassigned from" : "reassigned from " + oldCategoryId + " to";
                System.out.println("gRPC: Task '" + task.getTaskName() + "' " + action + " category " + (newCategoryId == 0 ? oldCategoryId : newCategoryId));
            }
        }
        
        ReassignCategoryResponse response = ReassignCategoryResponse.newBuilder()
            .setTasksModified(modifiedCount)
            .build();
        
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void createTask(CreateTaskRequest request, StreamObserver<TaskResponse> responseObserver) {
        responseObserver.onNext(TaskResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void updateTask(UpdateTaskRequest request, StreamObserver<TaskResponse> responseObserver) {
        responseObserver.onNext(TaskResponse.newBuilder().build());
        responseObserver.onCompleted();
    }

    @Override
    public void deleteTask(TaskIdRequest request, StreamObserver<EmptyResponse> responseObserver) {
        responseObserver.onNext(EmptyResponse.newBuilder().build());
        responseObserver.onCompleted();
    }
}
