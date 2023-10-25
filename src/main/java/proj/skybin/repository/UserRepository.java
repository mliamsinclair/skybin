package proj.skybin.repository;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import proj.skybin.model.User;
import java.util.Optional;


@EnableJpaRepositories
@Repository
public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmailAndPassword(String email, String password);
    User findByEmail(String email);
}
