package Backend.demo.Controllers;

import Backend.demo.Repositories.dashboard.TaskHistoryRepository;
import Backend.demo.Repositories.task.StatusRepository;
import Backend.demo.Repositories.task.TaskCategoryRepository;
import Backend.demo.Repositories.task.TasksRepository;
import Backend.demo.Repositories.worker.WorkerRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/viewer")
public class ViewerDataController {

    private final TasksRepository tasksRepository;
    private final WorkerRepository workerRepository;
    private final StatusRepository statusRepository;
    private final TaskCategoryRepository taskCategoryRepository;
    private final TaskHistoryRepository taskHistoryRepository;

    public ViewerDataController(
            TasksRepository tasksRepository,
            WorkerRepository workerRepository,
            StatusRepository statusRepository,
            TaskCategoryRepository taskCategoryRepository,
            TaskHistoryRepository taskHistoryRepository) {
        this.tasksRepository = tasksRepository;
        this.workerRepository = workerRepository;
        this.statusRepository = statusRepository;
        this.taskCategoryRepository = taskCategoryRepository;
        this.taskHistoryRepository = taskHistoryRepository;
    }

    @PostMapping("/reset")
    public Map<String, String> resetViewerData(@RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);

        taskHistoryRepository.deleteByViewerKey(viewerKey);
        tasksRepository.deleteByViewerKey(viewerKey);
        workerRepository.deleteByViewerKey(viewerKey);
        statusRepository.deleteByViewerKey(viewerKey);
        taskCategoryRepository.deleteByViewerKey(viewerKey);

        return Map.of("message", "Viewer data reset complete");
    }
}
