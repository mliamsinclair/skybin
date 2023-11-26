package proj.skybin.repository;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import proj.skybin.model.FolderInfo;
import java.util.Optional;
import java.util.List;

@EnableJpaRepositories
@Repository
public interface FolderRepository extends JpaRepository<FolderInfo, String>{
    Optional<FolderInfo> findByPath(String folderpath);
    Optional<FolderInfo> findByName(String name);
    List<FolderInfo> findByOwner(String owner);
    Optional<FolderInfo> findByDirectory(String directory);
    Optional<FolderInfo>findByOwnerAndDirectoryAndName(String owner, String directory, String name);
    List<FolderInfo> findByPathAndOwner(String folderpath, String owner);
    List<FolderInfo> findByDirectoryAndOwner(String directory, String owner);
}
