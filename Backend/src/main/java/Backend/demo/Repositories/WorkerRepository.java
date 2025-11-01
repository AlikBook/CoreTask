package Backend.demo.Repositories;
import Backend.demo.Entities.Worker;
import org.springframework.data.jpa.repository.JpaRepository;


public interface WorkerRepository extends JpaRepository<Worker, Integer> {
    // You can add custom query methods here if needed
}
