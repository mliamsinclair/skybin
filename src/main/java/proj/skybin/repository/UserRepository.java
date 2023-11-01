package proj.skybin.repository;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import proj.skybin.model.UserInfo;
import java.util.Optional;


@EnableJpaRepositories
@Repository
public interface UserRepository extends JpaRepository<UserInfo, Long>{
    Optional<UserInfo> findByUsernameAndPassword(String username, String password);
    Optional<UserInfo> findByUsername(String username);
}
