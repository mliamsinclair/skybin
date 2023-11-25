package proj.skybin.service;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proj.skybin.model.FolderInfo;
import proj.skybin.repository.FolderRepository;

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    public FolderInfo createFolder(FolderInfo folder) {
        // find parent folder
        Path parentPath = Paths.get(folder.getFolderpath()).getParent();
        // if parent folder exists, set parent directory
        if (parentPath != null) {
            folder.setParentpath(parentPath.toString());
        }
        return folderRepository.save(folder);
    }

    public FolderInfo getFolder(String folderpath) {
        return folderRepository.findByFolderpath(folderpath).orElse(null);
    }

    public FolderInfo getFolder(String owner, String directory, String foldername) {
        return folderRepository.findByOwnerAndDirectoryAndFoldername(owner, directory, foldername).orElse(null);
    }

    public List<FolderInfo> getHomeDirectoryContents(String owner) {
        return folderRepository.findByOwner(owner);
    }

    public List<FolderInfo> getSubFolders(String directory, String owner) {
        // get all folders in directory
        List<FolderInfo> folders = folderRepository.findByDirectoryAndOwner(directory, owner);
        // call recursive function to get all subfolders
        for (FolderInfo f : folders) {
            List<FolderInfo> subFolders = getSubFolders((f.getDirectory() + f.getFoldername() + "/"), owner);
            folders.addAll(subFolders);
        }
        return folders;
    }

    public void deleteFolder(String folderpath) {
        folderRepository.deleteById(folderpath);
    }

    public void updateFolder(FolderInfo folder) {
        folderRepository.save(folder);
    }


}
