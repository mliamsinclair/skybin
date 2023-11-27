package proj.skybin.service;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proj.skybin.model.FileInfo;
import proj.skybin.model.FolderInfo;
import proj.skybin.repository.FolderRepository;

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private FileService fileService;

    public FolderInfo createFolder(FolderInfo folder) {
        // find parent folder
        Path parentPath = Paths.get(folder.getPath()).getParent();
        // if parent folder exists, set parent directory
        Optional<FolderInfo> parent = folderRepository.findByPath(parentPath.toString());
        if (parent.isPresent()) {
            if (parent.get().getSubfolders() == null) {
                parent.get().setSubfolders(new ArrayList<>());
            }
            parent.get().getSubfolders().add(folder);
            folder.setParent(parent.get());
            folderRepository.save(parent.get());
        }
        return folderRepository.save(folder);
    }

    public FolderInfo getFolder(String folderpath) {
        return folderRepository.findByPath(folderpath).orElse(null);
    }

    public FolderInfo getFolder(String owner, String directory, String foldername) {
        return folderRepository.findByOwnerAndDirectoryAndName(owner, directory, foldername).orElse(null);
    }

    public List<FolderInfo> getHomeDirectoryContents(String owner) {
        return folderRepository.findByOwner(owner);
    }

    public void deleteFolder(String folderpath) {
        // remove folder from parent folder
        Path parentPath = Paths.get(folderpath).getParent();
        Optional<FolderInfo> parent = folderRepository.findByPath(parentPath.toString());
        if (parent.isPresent()) {
            parent.get().getSubfolders().removeIf(f -> f.getPath().equals(folderpath));
            folderRepository.save(parent.get());
        }
        // delete all subfolders
        FolderInfo folder = folderRepository.findByPath(folderpath).orElse(null);
        if (folder != null) {
            // delete all subfolders
            if (folder.getSubfolders() != null) {
                List<FolderInfo> subfoldersCopy = new ArrayList<>(folder.getSubfolders());
                for (FolderInfo subfolder : subfoldersCopy) {
                    deleteFolder(subfolder.getPath());
                }
                folder.getSubfolders().clear();
            }
            // delete all files
            if (folder.getFiles() != null) {
                List<FileInfo> filesCopy = new ArrayList<>(folder.getFiles());
                for (FileInfo file : filesCopy) {
                    fileService.deleteFile(file.getPath());
                }
                folder.getFiles().clear();
            }
        }
        // delete folder
        folderRepository.deleteById(folderpath);
    }

    public void updateFolder(FolderInfo folder) {
        folderRepository.save(folder);
    }

}
