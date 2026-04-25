package Backend.demo.Repositories.dashboard;

import Backend.demo.Entities.dashboard.TaskHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskHistoryRepository extends JpaRepository<TaskHistory, Integer> {
    List<TaskHistory> findTop20ByOrderByTimestampDesc();

    List<TaskHistory> findTop20ByViewerKeyOrderByTimestampDesc(String viewerKey);

    void deleteByViewerKey(String viewerKey);
}
