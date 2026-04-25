package Backend.demo.Controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import Backend.demo.Entities.task.TaskCategory;
import Backend.demo.Repositories.task.TaskCategoryRepository;
import Backend.demo.grpc.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

@RestController
@RequestMapping("/categories")
class TaskCategoryController {
    @Autowired
    private TaskCategoryRepository taskCategoryRepository;

    private final TaskServiceGrpc.TaskServiceBlockingStub taskGrpcClient;

    public TaskCategoryController() {
        ManagedChannel channel = ManagedChannelBuilder
                .forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        this.taskGrpcClient = TaskServiceGrpc.newBlockingStub(channel);
    }

    @GetMapping
    public List<TaskCategory> get_tasks_categories(@RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        List<TaskCategory> tasks_categories = taskCategoryRepository.findAllByViewerKey(viewerKey);

        return tasks_categories;
    }

    @GetMapping("/{id}")
    public TaskCategory get_task_category(@PathVariable Integer id,
            @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        return taskCategoryRepository.findByCategoryIdAndViewerKey(id, viewerKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "GET | Category with the id : " + id + " not found"));

    }

    @PostMapping
    public TaskCategory create_new_category(@RequestBody TaskCategory newCategory,
            @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        newCategory.setViewerKey(viewerKey);
        return taskCategoryRepository.save(newCategory);
    }

    @DeleteMapping("/{id}")
    public void delete_category(@PathVariable Integer id, @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        TaskCategory task_Category_to_delete = taskCategoryRepository.findByCategoryIdAndViewerKey(id, viewerKey)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));

        String categoryName = task_Category_to_delete.getCategoryName();

        try {
            ReassignCategoryRequest request = ReassignCategoryRequest.newBuilder()
                    .setOldCategoryId(id)
                    .setNewCategoryId(0)
                    .build();

            ReassignCategoryResponse response = taskGrpcClient.reassignTasksWithCategory(request);
            System.out.println("gRPC: Unassigned category from " + response.getTasksModified() + " task(s)");
        } catch (Exception e) {
            System.out.println("Warning: Failed to unassign category from tasks: " + e.getMessage());
        }

        taskCategoryRepository.delete(task_Category_to_delete);
        System.out.println("Deleted category: " + categoryName);
    }

    @PutMapping("/id")
    public TaskCategory modify_TaskCategory(@PathVariable Integer id, @RequestBody TaskCategory new_category,
            @RequestHeader("X-Viewer-Key") String rawViewerKey) {
        String viewerKey = ViewerKeyResolver.resolve(rawViewerKey);
        return taskCategoryRepository.findByCategoryIdAndViewerKey(id, viewerKey)
                .map(existing_category -> {
                    existing_category.setCategoryName(new_category.getCategoryName());
                    existing_category.setViewerKey(viewerKey);
                    return taskCategoryRepository.save(existing_category);
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PUT | Category not found"));

    }
}
