package proj.skybin.repository;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import proj.skybin.model.FileInfo;
import java.util.Optional;
import java.util.List;



@EnableJpaRepositories
@Repository
public interface FileRepository extends JpaRepository<FileInfo, String>{
    Optional<FileInfo> findByPath(String filepath);
    List<FileInfo> findByOwner(String owner);
    List<FileInfo> findByDirectory(String directory);
    List<FileInfo> findByDirectoryAndOwner(String directory, String owner);
    Optional<FileInfo> findByOwnerAndDirectoryAndName(String owner, String directory, String name);
    void deleteByPath(String filepath);
}
