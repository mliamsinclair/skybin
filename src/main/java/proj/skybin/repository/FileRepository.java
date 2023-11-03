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
    Optional<FileInfo> findByFilepath(String filepath);
    Optional<FileInfo> findByFilename(String filename);
    Optional<FileInfo> findByExtension(String extension);
    Optional<FileInfo> findByOwner(String owner);
    Optional<FileInfo> findByDirectory(String directory);
    List<FileInfo> findByDirectoryAndOwner(String directory, String owner);
}
