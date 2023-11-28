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
        String originalPath = f.getPath();
        // remove everything in the path after the owner's name
        String[] path = f.getPath().split(f.getOwner());
        if (path.length == 1) {
            f.setPath(f.getOwner());
        } else {
            f.setPath(f.getOwner());
            for (int i = 1; i < path.length; i++) {
                f.setPath(f.getPath() + path[i]);
            }
        }
        // find parent folder
        Path parentPath = Paths.get(originalPath).getParent();
        // if parent folder exists, set parent directory
        String[] parentPathSplit = parentPath.toString().split(f.getOwner());
        String parentPathString = "";
        if (parentPathSplit.length == 1) {
            parentPathString = f.getOwner();
        } else if (parentPathSplit.length > 1) {
            for (int i = 1; i < parentPathSplit.length; i++) {
                parentPathString = parentPathString + parentPathSplit[i];
            }
        }
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
}
