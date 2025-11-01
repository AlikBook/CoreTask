package Backend.demo.Controllers;
import java.util.List;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import Backend.demo.Repositories.WorkerRepository;
import Backend.demo.Entities.Worker;

@RestController
@RequestMapping("/workers")
public class WorkerController {

    @Autowired
    private WorkerRepository workerRepository;


    @GetMapping
    public List<Worker> getWorkers() {
        List<Worker> worker_list = workerRepository.findAll();
        if(worker_list.size() == 0){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "GET | No workers found");
        }
        return worker_list;        
    }

    @GetMapping("/{id}")
    public Worker getWorkerbyID(@PathVariable Integer id) {
        return workerRepository.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "GET | Worker with id : "+ id+ " not found"));
    }

    @PostMapping
    public Worker createWorker(@RequestBody Worker worker) {
        return workerRepository.save(worker);
    }

    @DeleteMapping("/{id}")
    public void deleteWorker(@PathVariable Integer id) {
        if (!workerRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "DELETE | Worker with id : "+id + " not found");
        }
        workerRepository.deleteById(id);
    }

    @PutMapping("/{id}")
    public Worker updateWorker(@PathVariable Integer id, @RequestBody Worker updatedWorker) {
        return workerRepository.findById(id)
        .map(existingWorker -> {
            existingWorker.setWorkerName(updatedWorker.getWorkerName());
            existingWorker.setWorkerLastName(updatedWorker.getWorkerLastName());
            return workerRepository.save(existingWorker);
        })
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PUT | Worker with id :"+ id +" not found"));
    }

}
