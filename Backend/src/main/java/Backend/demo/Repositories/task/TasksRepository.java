package Backend.demo.Repositories.task;

import Backend.demo.Entities.task.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TasksRepository extends JpaRepository<Tasks, Integer> {
    
}
