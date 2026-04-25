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

    private final TaskServiceGrpc.TaskServiceBlockingStub taskGrpcClient;

    public TaskStatusController() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        this.taskGrpcClient = TaskServiceGrpc.newBlockingStub(channel);
    }

    @GetMapping
    public List<TaskStatus> getAllStatuses(@RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        return statusRepository.findAllByViewerKey(viewerKey);
    }

    @GetMapping("/{id}")
    public TaskStatus getStatusById(@PathVariable Integer id, @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        return statusRepository.findByStatusIdAndViewerKey(id, viewerKey)
                .orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Status with id " + id + " not found"));
    }

    @PostMapping
    public TaskStatus createStatus(@RequestBody TaskStatus status, @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        status.setViewerKey(viewerKey);
        return statusRepository.save(status);
    }

    @PutMapping("/{id}")
    public TaskStatus updateStatus(@PathVariable Integer id, @RequestBody TaskStatus updatedStatus,
            @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        TaskStatus existingStatus = statusRepository.findByStatusIdAndViewerKey(id, viewerKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Status with id " + id + " not found for update"));
        existingStatus.setStatusName(updatedStatus.getStatusName());
        existingStatus.setViewerKey(viewerKey);
        return statusRepository.save(existingStatus);
    }

    @DeleteMapping("/{id}")
    public void deleteStatus(@PathVariable Integer id, @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        TaskStatus statusToDelete = statusRepository.findByStatusIdAndViewerKey(id, viewerKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Status with id " + id + " not found for deletion"));

        String statusName = statusToDelete.getStatusName();

        try {
            ReassignStatusRequest request = ReassignStatusRequest.newBuilder()
                    .setOldStatusId(id)
                    .setNewStatusId(0)
                    .build();

            ReassignStatusResponse response = taskGrpcClient.reassignTasksWithStatus(request);
            System.out.println("gRPC: Unassigned status from " + response.getTasksModified() + " task(s)");
        } catch (Exception e) {
            System.out.println("Warning: Failed to unassign status from tasks: " + e.getMessage());
        }

        statusRepository.delete(statusToDelete);
        System.out.println("Deleted status: " + statusName);
    }
}