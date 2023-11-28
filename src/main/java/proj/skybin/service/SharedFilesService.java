package proj.skybin.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import proj.skybin.repository.SharedFilesRepository;
import proj.skybin.model.SharedFiles;
import java.util.List;

@Service
public class SharedFilesService {

    @Autowired
    private SharedFilesRepository sharedFilesRepository;

    public SharedFiles share(SharedFiles sharedFiles) {
        return sharedFilesRepository.save(sharedFiles);
    }

    public List<SharedFiles> getSharedFiles(String sharedUser) {
        return sharedFilesRepository.findBySharedUser(sharedUser);
    }

    public boolean unshare(String path, String owner, String sharedUser) {
        Optional<SharedFiles> sharedFile = sharedFilesRepository.findByPathAndOwnerAndSharedUser(path, owner,
                sharedUser);
        if (sharedFile.isPresent()) {
            sharedFilesRepository.delete(sharedFile.get());
            return true;
        }
        return false;
    }
}