package proj.skybin.service;

import proj.skybin.model.FileInfo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import proj.skybin.repository.FileRepository;

@Service
public class FileService {

    @Autowired
    private FileRepository fileRepository;

    public FileInfo createFile(FileInfo f) {
        fileRepository.save(f);
        return f;
    }

    public Optional<FileInfo> getFile(String filepath) {
        return fileRepository.findByFilepath(filepath);
    }

    public List<FileInfo> getDirectoryContents(String directory, String owner) {
        return fileRepository.findByDirectoryAndOwner(directory, owner);
    }
}
