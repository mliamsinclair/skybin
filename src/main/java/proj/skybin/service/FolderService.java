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
        Path parentPath = Paths.get(System.getProperty("user.dir"), "filedir", folder.getOwner(), folder.getDirectory());
        String parentPathString = parentPath.toString();
        Optional<FolderInfo> parent = folderRepository.findByFolderpath(parentPathString);
        if (parent.isPresent()) {
            String[] parentFolders = parent.get().getFolders();
            if (parentFolders == null) {
                parentFolders = new String[1];
                parentFolders[0] = folder.getFoldername();
            } else {
                String[] newFolders = new String[parentFolders.length + 1];
                for (int i = 0; i < parentFolders.length; i++) {
                    newFolders[i] = parentFolders[i];
                }
                newFolders[parentFolders.length] = folder.getFoldername();
                parentFolders = newFolders;
            }
            parent.get().setFolders(parentFolders);
            folderRepository.save(parent.get());
        }
        folderRepository.save(folder);
        return folder;
    }

    public FolderInfo getFolder(String folderpath) {
        return folderRepository.findByFolderpath(folderpath).orElse(null);
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
