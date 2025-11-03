package Backend.demo.Repositories.worker;

import Backend.demo.Entities.worker.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    // You can add custom query methods here if needed
}
