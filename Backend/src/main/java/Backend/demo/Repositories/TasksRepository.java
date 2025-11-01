package Backend.demo.Repositories;


import Backend.demo.Entities.Tasks;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TasksRepository extends JpaRepository<Tasks, Integer> {
    
    
}
