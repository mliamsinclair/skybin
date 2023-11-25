package proj.skybin.service;

import proj.skybin.model.FileInfo;
import proj.skybin.model.FolderInfo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proj.skybin.repository.FileRepository;
import proj.skybin.repository.FolderRepository;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    public FileInfo createFile(FileInfo f) {
        // find parent folder
        String parentPath = f.getFilepath().substring(0, f.getFilepath().lastIndexOf("/"));
        // if parent folder exists, set parent directory
        Optional<FolderInfo> parent = folderRepository.findByFolderpath(parentPath);
        if (parent.isPresent()) {
            f.setParent(parent.get());
        }
        return fileRepository.save(f);
    }

    public Optional<FileInfo> getFile(String filepath) {
        return fileRepository.findByFilepath(filepath);
    }

    public FileInfo getFile(String owner, String directory, String filename) {
        return fileRepository.findByOwnerAndDirectoryAndFilename(owner, directory, filename).orElse(null);
    }

    public List<FileInfo> getDirectoryContents(String directory, String owner) {
        return fileRepository.findByDirectoryAndOwner(directory, owner);
    }

    public List<FileInfo> getAllFiles(String owner) {
        return fileRepository.findByOwner(owner);
    }

    public void deleteFile(String filepath) {
        fileRepository.deleteByFilepath(filepath);
    }
}
