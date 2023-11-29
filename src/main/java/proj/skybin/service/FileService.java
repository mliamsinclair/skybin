package proj.skybin.service;

import proj.skybin.model.FileInfo;
import proj.skybin.model.FolderInfo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import proj.skybin.repository.FileRepository;
import proj.skybin.repository.FolderRepository;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    public FileInfo createFile(FileInfo f) {
        String pathString = "";
        String parentPathString = "";
        // remove everything in the path after the owner's name
        String[] path = f.getPath().split(f.getOwner());
        if (path.length == 1) {
            pathString = f.getOwner();
            f.setPath(pathString);
        } else {
            pathString = f.getOwner();
            for (int i = 1; i < path.length; i++) {
                pathString = pathString + path[i];
            }
            f.setPath(pathString);
        }
        // find parent folder
        Path parentPath = Paths.get(pathString).getParent();
        // if parent folder exists, set parent directory
        if (parentPath != null) {
            parentPathString = parentPath.toString();
            Optional<FolderInfo> parent = folderRepository.findByPath(parentPathString);
            if (parent.isPresent()) {
                f.setParent(parent.get());
                if (parent.get().getFiles() == null) {
                    parent.get().setFiles(new java.util.ArrayList<>());
                }
                parent.get().getFiles().add(f);
                folderRepository.save(parent.get());
                f.setParentpath(parentPath.toString());
            }
        }
        return fileRepository.save(f);
    }

    public Optional<FileInfo> getFile(String filepath) {
        return fileRepository.findByPath(filepath);
    }

    public FileInfo getFile(String owner, String directory, String filename) {
        return fileRepository.findByOwnerAndDirectoryAndName(owner, directory, filename).orElse(null);
    }

    public List<FileInfo> getDirectoryContents(String directory, String owner) {
        return fileRepository.findByDirectoryAndOwner(directory, owner);
    }

    public List<FileInfo> getAllFiles(String owner) {
        return fileRepository.findByOwner(owner);
    }

    @Transactional
    public void deleteFile(String filepath) {
        // remove file from parent folder
        Path parentPath = Paths.get(filepath).getParent();
        Optional<FolderInfo> parent = folderRepository.findByPath(parentPath.toString());
        if (parent.isPresent()) {
            parent.get().getFiles().removeIf(f -> f.getPath().equals(filepath));
            folderRepository.save(parent.get());
        }
        fileRepository.deleteByPath(filepath);
    }

    // update file name
    // this will update the file's name
    // this will also update the file's path
    public boolean renameFile(String filepath, String newName) {
        Optional<FileInfo> file = fileRepository.findByPath(filepath);
        if (file.isPresent()) {
            // update file name
            file.get().setName(newName);
            // update file path
            String newPath = filepath.substring(0, filepath.lastIndexOf("/") + 1) + newName;
            file.get().setPath(newPath);
            fileRepository.save(file.get());
            return true;
        }
        return false;
    }
}
