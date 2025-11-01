package Backend.demo.Repositories;

import Backend.demo.Entities.TaskCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Integer> {
    // You can add custom query methods here if needed
}
