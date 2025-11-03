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
    
    // gRPC client to check tasks
    private final TaskServiceGrpc.TaskServiceBlockingStub taskGrpcClient;
    
    public TaskCategoryController() {
        ManagedChannel channel = ManagedChannelBuilder
            .forAddress("localhost", 9090)
            .usePlaintext()
            .build();
        this.taskGrpcClient = TaskServiceGrpc.newBlockingStub(channel);
    }

    @GetMapping
    public List<TaskCategory> get_tasks_categories(){
        List<TaskCategory> tasks_categories = taskCategoryRepository.findAll();
        if(tasks_categories.isEmpty()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GET | No categories found");
        }
        return tasks_categories;
    }

    @GetMapping("/{id}")
    public TaskCategory get_task_category(@PathVariable Integer id){
        return taskCategoryRepository.findById(id)
        .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "GET | Category with the id : "+ id+ " not found"));
        
    }

    @PostMapping
    public TaskCategory create_new_category(@RequestBody TaskCategory newCategory){
        return taskCategoryRepository.save(newCategory);
    }

    @DeleteMapping("/{id}")
    public void delete_category(@PathVariable Integer id ){
        if(!taskCategoryRepository.existsById(id)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"DELETE | category not found");
        }
        
        // Check via gRPC if category is being used by tasks
        try {
            EmptyRequest request = EmptyRequest.newBuilder().build();
            TaskListResponse tasks = taskGrpcClient.getAllTasks(request);
            
            long taskCount = tasks.getTasksList().stream()
                .filter(task -> task.getCategoryId() == id)
                .count();
            
            if (taskCount > 0) {
                System.out.println("⚠ gRPC: Category is used by " + taskCount + " task(s), but proceeding with deletion");
            } else {
                System.out.println("✓ gRPC: Category is not used by any tasks, safe to delete");
            }
        } catch (Exception e) {
            System.out.println("⚠ gRPC check failed, proceeding with deletion anyway");
        }
        
        taskCategoryRepository.deleteById(id);
    }

    @PutMapping("/id")
    public TaskCategory modify_TaskCategory(@PathVariable Integer id, @RequestBody TaskCategory new_category){
        return taskCategoryRepository.findById(id)
        .map(existing_category ->{
            existing_category.setCategoryName(new_category.getCategoryName());
            return taskCategoryRepository.save(existing_category);
        })
        .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND, "PUT | Category not found"));
        
    }
}
