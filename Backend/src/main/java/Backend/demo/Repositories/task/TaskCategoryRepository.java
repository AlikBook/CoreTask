package Backend.demo.Repositories.task;

import Backend.demo.Entities.task.TaskCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Integer> {
    List<TaskCategory> findAllByViewerKey(String viewerKey);

    Optional<TaskCategory> findByCategoryIdAndViewerKey(Integer categoryId, String viewerKey);

    void deleteByViewerKey(String viewerKey);
}
