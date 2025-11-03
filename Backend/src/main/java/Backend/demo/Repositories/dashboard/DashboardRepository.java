package Backend.demo.Repositories.dashboard;

import Backend.demo.Entities.dashboard.Dashboard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, Integer> {
}
