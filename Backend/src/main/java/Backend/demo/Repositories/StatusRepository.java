package Backend.demo.Repositories;

import Backend.demo.Entities.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StatusRepository extends JpaRepository<TaskStatus, Integer> {
    // You can add custom query methods here if needed
}
