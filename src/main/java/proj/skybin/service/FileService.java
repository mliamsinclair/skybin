package proj.skybin.service;

import proj.skybin.model.FileInfo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.nio.file.Paths;
import proj.skybin.repository.FileRepository;
import proj.skybin.repository.FolderRepository;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FolderRepository folderRepository;

    public FileInfo createFile(FileInfo f) {
        Path parentPath = Paths.get(System.getProperty("user.dir"), "filedir", f.getOwner(), f.getDirectory());
        folderRepository.findByFolderpath(parentPath.toString()).ifPresent(folder -> {
            String[] parentFiles = folder.getFiles();
            if (parentFiles == null) {
                parentFiles = new String[1];
                parentFiles[0] = f.getFilename();
            } else {
                String[] newFiles = new String[parentFiles.length + 1];
                for (int i = 0; i < parentFiles.length; i++) {
                    newFiles[i] = parentFiles[i];
                }
                newFiles[parentFiles.length] = f.getFilename();
                parentFiles = newFiles;
            }
            folder.setFiles(parentFiles);
            folderRepository.save(folder);
        });
        return f;
    }

    public Optional<FileInfo> getFile(String filepath) {
        return fileRepository.findByFilepath(filepath);
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
