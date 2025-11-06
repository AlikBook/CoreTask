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
        TaskStatus statusToDelete = statusRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status with id " + id + " not found for deletion"));
        
        statusRepository.delete(statusToDelete);
        System.out.println("Deleted status: " + statusToDelete.getStatusName());
    }
}