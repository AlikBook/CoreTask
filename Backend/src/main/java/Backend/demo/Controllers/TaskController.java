package Backend.demo.Controllers;

import Backend.demo.Entities.Tasks;
import Backend.demo.Repositories.TasksRepository;
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

    @GetMapping
    public List<Tasks> getAllTasks() {
        List<Tasks> taskList = tasksRepository.findAll();
        if (taskList.size() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GET | No tasks found");
        }
        return taskList;
    }

    @GetMapping("/{id}")
    public Tasks getTaskById(@PathVariable Integer id) {
        return tasksRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "GET | Task with id " + id + " not found"));
    }

    @PostMapping
    public Tasks createTask(@RequestBody Tasks task) {
        return tasksRepository.save(task);
    }

    @PutMapping("/{id}")
    public Tasks updateTask(@PathVariable Integer id, @RequestBody Tasks updatedTask) {
        return tasksRepository.findById(id)
            .map(existingTask -> {
                existingTask.setTaskName(updatedTask.getTaskName());
                existingTask.setDueDate(updatedTask.getDueDate());
                existingTask.setDescription(updatedTask.getDescription());
                existingTask.setWorker(updatedTask.getWorker());
                existingTask.setStatus(updatedTask.getStatus());
                existingTask.setCategory(updatedTask.getCategory());
                return tasksRepository.save(existingTask);
            })
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PUT | Task with id " + id + " not found"));
    }

    @DeleteMapping("/{id}")
    public void deleteTask(@PathVariable Integer id) {
        if (!tasksRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DELETE | Task with id " + id + " not found");
        }
        tasksRepository.deleteById(id);
    }
}