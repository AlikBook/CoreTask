package Backend.demo.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import Backend.demo.Repositories.worker.WorkerRepository;
import Backend.demo.Entities.worker.Worker;
import Backend.demo.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@RestController
@RequestMapping("/workers")
public class WorkerController {

    @Autowired
    private WorkerRepository workerRepository;

    private final TaskServiceGrpc.TaskServiceBlockingStub taskGrpcClient;
    private final DashboardServiceGrpc.DashboardServiceBlockingStub dashboardGrpcClient;

    public WorkerController() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        this.taskGrpcClient = TaskServiceGrpc.newBlockingStub(channel);
        this.dashboardGrpcClient = DashboardServiceGrpc.newBlockingStub(channel);
    }

    @GetMapping
    public List<Worker> getWorkers(@RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        List<Worker> worker_list = workerRepository.findAllByViewerKey(viewerKey);

        return worker_list;
    }

    @GetMapping("/{id}")
    public Worker getWorkerbyID(@PathVariable Integer id, @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        return workerRepository.findByWorkerIdAndViewerKey(id, viewerKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "GET | Worker with id : " + id + " not found"));
    }

    @PostMapping
    public Worker createWorker(@RequestBody Worker worker, @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        worker.setViewerKey(viewerKey);
        Worker savedWorker = workerRepository.save(worker);

        try {
            WorkerChangeRequest request = WorkerChangeRequest.newBuilder()
                    .setAction("CREATE")
                    .setWorkerName(savedWorker.getWorkerName() + " " + savedWorker.getWorkerLastName())
                    .setDetails("VIEWER=" + viewerKey + "|Worker created with ID: " + savedWorker.getWorkerId())
                    .build();
            dashboardGrpcClient.notifyWorkerChange(request);
        } catch (Exception e) {
            System.out.println("Dashboard notification failed: " + e.getMessage());
        }

        return savedWorker;
    }

    @DeleteMapping("/{id}")
    public void deleteWorker(@PathVariable Integer id, @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        Worker worker = workerRepository.findByWorkerIdAndViewerKey(id, viewerKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "DELETE | Worker with id : " + id + " not found"));

        String workerName = worker.getWorkerName() + " " + worker.getWorkerLastName();

        try {
            UnassignWorkerRequest request = UnassignWorkerRequest.newBuilder()
                    .setWorkerId(id)
                    .build();
            UnassignWorkerResponse response = taskGrpcClient.unassignWorkerFromTasks(request);

            if (response.getTasksModified() > 0) {
                System.out.println("gRPC: Unassigned worker from " + response.getTasksModified() + " task(s)");
            } else {
                System.out.println("gRPC: Worker has no assigned tasks");
            }
        } catch (Exception e) {
            System.out.println("gRPC unassign failed: " + e.getMessage() + ", proceeding with deletion anyway");
        }

        workerRepository.deleteById(id);

        try {
            WorkerChangeRequest request = WorkerChangeRequest.newBuilder()
                    .setAction("DELETE")
                    .setWorkerName(workerName)
                    .setDetails("VIEWER=" + viewerKey + "|Worker deleted with ID: " + id)
                    .build();
            dashboardGrpcClient.notifyWorkerChange(request);
        } catch (Exception e) {
            System.out.println("Dashboard notification failed: " + e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public Worker updateWorker(@PathVariable Integer id, @RequestBody Worker updatedWorker,
            @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        return workerRepository.findByWorkerIdAndViewerKey(id, viewerKey)
                .map(existingWorker -> {
                    existingWorker.setWorkerName(updatedWorker.getWorkerName());
                    existingWorker.setWorkerLastName(updatedWorker.getWorkerLastName());
                    existingWorker.setViewerKey(viewerKey);
                    return workerRepository.save(existingWorker);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "PUT | Worker with id :" + id + " not found"));
    }
}
