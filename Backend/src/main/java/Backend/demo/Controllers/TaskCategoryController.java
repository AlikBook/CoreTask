package Backend.demo.Controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException.NotFound;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import Backend.demo.Entities.task.TaskCategory;
import Backend.demo.Repositories.task.TaskCategoryRepository;


@RestController
@RequestMapping("/categories")
class TaskCategoryController {
    @Autowired
    private TaskCategoryRepository taskCategoryRepository;
    

    @GetMapping
    public List<TaskCategory> get_tasks_categories(){
        List<TaskCategory> tasks_categories = taskCategoryRepository.findAll();
    
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
    public void delete_category(@PathVariable Integer id) {
        TaskCategory task_Category_to_delete = taskCategoryRepository.findById(id)
        .orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found"));
        taskCategoryRepository.delete(task_Category_to_delete);
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
