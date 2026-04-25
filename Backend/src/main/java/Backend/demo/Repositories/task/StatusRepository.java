package Backend.demo.Repositories.task;

import Backend.demo.Entities.task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StatusRepository extends JpaRepository<TaskStatus, Integer> {
    List<TaskStatus> findAllByViewerKey(String viewerKey);

    Optional<TaskStatus> findByStatusIdAndViewerKey(Integer statusId, String viewerKey);

    void deleteByViewerKey(String viewerKey);
}
