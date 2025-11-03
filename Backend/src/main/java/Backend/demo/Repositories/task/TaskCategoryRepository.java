package Backend.demo.Repositories.task;

import Backend.demo.Entities.task.TaskCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Integer> {
}
