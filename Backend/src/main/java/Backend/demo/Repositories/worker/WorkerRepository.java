package Backend.demo.Repositories.worker;

import Backend.demo.Entities.worker.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WorkerRepository extends JpaRepository<Worker, Integer> {
    List<Worker> findAllByViewerKey(String viewerKey);

    Optional<Worker> findByWorkerIdAndViewerKey(Integer workerId, String viewerKey);

    long countByViewerKey(String viewerKey);

    void deleteByViewerKey(String viewerKey);
}
