package Backend.demo.Controllers;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import Backend.demo.Entities.TaskCategory;
import Backend.demo.Repositories.TaskCategoryRepository;

@RestController
@RequestMapping("/task_category")
class TaskCategoryController {
    @Autowired
    private TaskCategoryRepository taskCategoryRepository;

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
}
