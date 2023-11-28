package proj.skybin.repository;

import java.util.List;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import proj.skybin.model.SharedFiles;

@EnableJpaRepositories
@Repository
public interface SharedFilesRepository extends JpaRepository<SharedFiles, String> {
    List<SharedFiles> findBySharedUser(String sharedUser);
    Optional<SharedFiles> findByPathAndOwnerAndSharedUser(String path, String owner, String sharedUser);
}
