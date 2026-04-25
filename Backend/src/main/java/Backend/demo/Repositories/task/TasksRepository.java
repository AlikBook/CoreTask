package Backend.demo.Repositories.task;

import Backend.demo.Entities.task.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TasksRepository extends JpaRepository<Tasks, Integer> {
    List<Tasks> findAllByViewerKey(String viewerKey);

    Optional<Tasks> findByTaskIdAndViewerKey(Integer taskId, String viewerKey);

    long countByViewerKey(String viewerKey);

    void deleteByViewerKey(String viewerKey);
}
