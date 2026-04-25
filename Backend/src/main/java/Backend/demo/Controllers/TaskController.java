package Backend.demo.Controllers;

import Backend.demo.Entities.task.Tasks;
import Backend.demo.Repositories.task.StatusRepository;
import Backend.demo.Repositories.task.TaskCategoryRepository;
import Backend.demo.Repositories.task.TasksRepository;
import Backend.demo.Repositories.worker.WorkerRepository;
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
    private WorkerRepository workerRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private TaskCategoryRepository taskCategoryRepository;

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
    public List<Tasks> getAllTasks(@RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        List<Tasks> taskList = tasksRepository.findAllByViewerKey(viewerKey);

        return taskList;
    }

    @GetMapping("/{id}")
    public Tasks getTaskById(@PathVariable Integer id, @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        return tasksRepository.findByTaskIdAndViewerKey(id, viewerKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "GET | Task with id " + id + " not found"));
    }

    @PostMapping
    public Tasks createTask(@RequestBody Tasks task, @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        task.setViewerKey(viewerKey);

        if (task.getWorkerId() != null && task.getWorkerId() != 0) {
            boolean workerExists = workerRepository.findByWorkerIdAndViewerKey(task.getWorkerId(), viewerKey)
                    .isPresent();
            if (!workerExists) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Worker with id " + task.getWorkerId() + " not found");
            }
        } else {
            System.out.println("Task created without assigned worker");
        }

        if (task.getStatusId() != null && task.getStatusId() != 0) {
            boolean statusExists = statusRepository.findByStatusIdAndViewerKey(task.getStatusId(), viewerKey)
                    .isPresent();
            if (!statusExists) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Status with id " + task.getStatusId() + " not found");
            }
        }

        if (task.getCategoryId() != null && task.getCategoryId() != 0) {
            boolean categoryExists = taskCategoryRepository
                    .findByCategoryIdAndViewerKey(task.getCategoryId(), viewerKey).isPresent();
            if (!categoryExists) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Category with id " + task.getCategoryId() + " not found");
            }
        }

        Tasks savedTask = tasksRepository.save(task);

        notifyDashboardTaskChange("CREATE", savedTask, viewerKey);

        return savedTask;
    }

    @PutMapping("/{id}")
    public Tasks updateTask(@PathVariable Integer id, @RequestBody Tasks updatedTask,
            @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        return tasksRepository.findByTaskIdAndViewerKey(id, viewerKey)
                .map(existingTask -> {
                    if (updatedTask.getWorkerId() != null && updatedTask.getWorkerId() != 0) {
                        boolean workerExists = workerRepository
                                .findByWorkerIdAndViewerKey(updatedTask.getWorkerId(), viewerKey).isPresent();
                        if (!workerExists) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Worker not found");
                        }
                    }

                    if (updatedTask.getStatusId() != null && updatedTask.getStatusId() != 0) {
                        boolean statusExists = statusRepository
                                .findByStatusIdAndViewerKey(updatedTask.getStatusId(), viewerKey).isPresent();
                        if (!statusExists) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status not found");
                        }
                        existingTask.setStatusId(updatedTask.getStatusId());
                    }

                    if (updatedTask.getCategoryId() != null && updatedTask.getCategoryId() != 0) {
                        boolean categoryExists = taskCategoryRepository
                                .findByCategoryIdAndViewerKey(updatedTask.getCategoryId(), viewerKey).isPresent();
                        if (!categoryExists) {
                            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Category not found");
                        }
                        existingTask.setCategoryId(updatedTask.getCategoryId());
                    }

                    existingTask.setTaskName(updatedTask.getTaskName());
                    existingTask.setDueDate(updatedTask.getDueDate());
                    existingTask.setDescription(updatedTask.getDescription());
                    existingTask.setWorkerId(updatedTask.getWorkerId());
                    existingTask.setViewerKey(viewerKey);

                    Tasks saved = tasksRepository.save(existingTask);

                    notifyDashboardTaskChange("UPDATE", saved, viewerKey);

                    return saved;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "PUT | Task with id " + id + " not found"));
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Integer id, @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        Tasks task = tasksRepository.findByTaskIdAndViewerKey(id, viewerKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "DELETE | Task with id " + id + " not found"));

        String taskName = task.getTaskName();

        tasksRepository.deleteById(id);

        try {
            TaskChangeRequest request = TaskChangeRequest.newBuilder()
                    .setAction("DELETE")
                    .setTaskName(taskName)
                    .setDetails("VIEWER=" + viewerKey + "|Task deleted with ID: " + id)
                    .build();
            dashboardGrpcClient.notifyTaskChange(request);
        } catch (Exception e) {
            System.out.println("Dashboard notification failed: " + e.getMessage());
        }

    }

    private void notifyDashboardTaskChange(String action, Tasks task, String viewerKey) {
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
            requestBuilder.setDetails("VIEWER=" + viewerKey + "|" + details);

            dashboardGrpcClient.notifyTaskChange(requestBuilder.build());
        } catch (Exception e) {
            System.out.println("Dashboard notification failed: " + e.getMessage());
        }
    }
}