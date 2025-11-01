package Backend.demo.Controllers;

import Backend.demo.Entities.Tasks;
import Backend.demo.Repositories.TasksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/tasks") 
class TasksController {

    @Autowired
    private TasksRepository tasksRepository;

    // Get all tasks
    @GetMapping
    public List<Tasks> getAllTasks() {
        return tasksRepository.findAll();
    }

    // Get a task by ID
    @GetMapping("/{id}")
    public Optional<Tasks> getTaskById(@PathVariable Integer id) {
        return tasksRepository.findById(id);
    }

    // Create a new task
    @PostMapping
    public Tasks createTask(@RequestBody Tasks task) {
        return tasksRepository.save(task);
    }

    // Update a task
    @PutMapping("/{id}")
    public Tasks updateTask(@PathVariable Integer id, @RequestBody Tasks updatedTask) {
        updatedTask.setTaskId(id);
        return tasksRepository.save(updatedTask);
    }

    // Delete a task
    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Integer id) {
        tasksRepository.deleteById(id);
    }
}