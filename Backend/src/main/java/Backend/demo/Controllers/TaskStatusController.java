package Backend.demo.Controllers;

import Backend.demo.Entities.task.TaskStatus;
import Backend.demo.Repositories.task.StatusRepository;
import Backend.demo.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.List;

@RestController
@RequestMapping("/statuses")
public class TaskStatusController {
    @Autowired
    private StatusRepository statusRepository;
    
    // gRPC client to check tasks
    private final TaskServiceGrpc.TaskServiceBlockingStub taskGrpcClient;
    
    public TaskStatusController() {
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 9090)
            .usePlaintext()
            .build();
        this.taskGrpcClient = TaskServiceGrpc.newBlockingStub(channel);
    }

    @GetMapping
    public List<TaskStatus> getAllStatuses() {
        return statusRepository.findAll();
    }

    @GetMapping("/{id}")
    public TaskStatus getStatusById(@PathVariable Integer id) {
        return statusRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status with id " + id + " not found"));
    }

    @PostMapping
    public TaskStatus createStatus(@RequestBody TaskStatus status) {
        return statusRepository.save(status);
    }

    @PutMapping("/{id}")
    public TaskStatus updateStatus(@PathVariable Integer id, @RequestBody TaskStatus updatedStatus) {
        TaskStatus existingStatus = statusRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status with id " + id + " not found for update"));
        existingStatus.setStatusName(updatedStatus.getStatusName());
        return statusRepository.save(existingStatus);
    }

    @DeleteMapping("/{id}")
    public void deleteStatus(@PathVariable Integer id) {
        if (!statusRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Status with id " + id + " not found for deletion");
        }
        
        // Check via gRPC if status is being used by tasks
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            TaskListResponse tasks = taskGrpcClient.getAllTasks(request);
            
            long taskCount = tasks.getTasksList().stream()
                .filter(task -> task.getStatusId() == id)
                .count();
            
            if (taskCount > 0) {
                System.out.println("⚠ gRPC: Status is used by " + taskCount + " task(s), but proceeding with deletion");
            } else {
                System.out.println("✓ gRPC: Status is not used by any tasks, safe to delete");
            }
        } catch (Exception e) {
            System.out.println("⚠ gRPC check failed, proceeding with deletion anyway");
        }
        
        statusRepository.deleteById(id);
    }
}