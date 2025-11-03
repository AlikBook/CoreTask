package Backend.demo.Repositories.task;

import Backend.demo.Entities.task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<TaskStatus, Integer> {
}
