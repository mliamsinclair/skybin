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
        // remove everything in the path after the owner's name
        String[] path = folder.getPath().split(folder.getOwner());
        String pathString = "";
        String parentPathString = "";
        if (path.length == 1) {
            pathString = folder.getOwner();
            folder.setPath(pathString);
        } else {
            pathString = folder.getOwner();
            for (int i = 1; i < path.length; i++) {
                pathString = pathString + path[i];
            }
            folder.setPath(pathString);
        }
        // find parent folder
        Path parentPath = Paths.get(pathString).getParent();
        if (parentPath != null) {
            parentPathString = parentPath.toString();
            Optional<FolderInfo> parent = folderRepository.findByPath(parentPathString);
            if (parent.isPresent()) {
                if (parent.get().getSubfolders() == null) {
                    parent.get().setSubfolders(new ArrayList<>());
                }
                parent.get().getSubfolders().add(folder);
                folder.setParent(parent.get());
                folderRepository.save(parent.get());
            }
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

    public FolderInfo getFolderByPath(String path) {
        return folderRepository.findByPath(path).orElse(null);
    }

    // rename folder
    // this will rename the folder
    // this will also rename the folder's directory
    // this will also rename the folder's path
    public boolean renameFolder(String folderpath, String newFoldername) {
        FolderInfo folder = folderRepository.findByPath(folderpath).orElse(null);
        if (folder != null) {
            // rename folder
            folder.setName(newFoldername);
            // rename directory
            String[] path = folder.getDirectory().split(folder.getName());
            String directory = "";
            if (path.length == 1) {
                directory = folder.getName();
            } else {
                directory = folder.getName();
                for (int i = 1; i < path.length; i++) {
                    directory = directory + path[i];
                }
            }
            folder.setDirectory(directory);
            // rename path
            String[] path2 = folder.getPath().split(folder.getName());
            String pathString = "";
            if (path2.length == 1) {
                pathString = folder.getName();
            } else {
                pathString = folder.getName();
                for (int i = 1; i < path2.length; i++) {
                    pathString = pathString + path2[i];
                }
            }
            folder.setPath(pathString);
            folderRepository.save(folder);
            return true;
        }
        return false;
    }
}
