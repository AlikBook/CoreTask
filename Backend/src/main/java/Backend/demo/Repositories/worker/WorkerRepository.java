package Backend.demo.Repositories.worker;

import Backend.demo.Entities.worker.Worker;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkerRepository extends JpaRepository<Worker, Integer> {
    // You can add custom query methods here if needed
}
