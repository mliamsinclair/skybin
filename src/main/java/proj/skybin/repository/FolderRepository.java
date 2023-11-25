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
    Optional<FolderInfo> findByFolderpath(String folderpath);
    Optional<FolderInfo> findByFoldername(String foldername);
    List<FolderInfo> findByOwner(String owner);
    Optional<FolderInfo> findByDirectory(String directory);
    Optional<FolderInfo>findByOwnerAndDirectoryAndFoldername(String owner, String directory, String foldername);
    List<FolderInfo> findByFolderpathAndOwner(String folderpath, String owner);
    List<FolderInfo> findByDirectoryAndOwner(String directory, String owner);
}
