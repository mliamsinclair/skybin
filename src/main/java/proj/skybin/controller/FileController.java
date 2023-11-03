package proj.skybin.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import proj.skybin.model.FileInfo;
import proj.skybin.service.FileService;

@RestController
@RequestMapping("/api/user")
public class FileController {

    @Autowired
    private FileService fileService;

    // for testing the addition of new files to the database, can also be used to
    // create a newfile without an upload (?)
    @PostMapping("/newfile")
    public FileInfo createFile(Principal principal, @RequestBody FileInfo f) {
        String owner = principal.getName();
        f.setOwner(owner);
        return fileService.createFile(f);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(Principal principal, @RequestParam String directory,
            @RequestParam MultipartFile file) throws IOException {
        if (directory == null) {
            directory = "";
        }
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName(), directory,
                file.getOriginalFilename());
        if (Files.exists(path)) {
            return ResponseEntity.badRequest().body("File already exists");
        }
        Files.copy(file.getInputStream(), path);
        FileInfo f = new FileInfo();
        f.setOwner(principal.getName());
        String filename = file.getOriginalFilename();
        f.setFilename(filename);
        f.setDirectory(directory);
        f.setFilepath(path.toString());
        if (filename != null) {
            if (filename.lastIndexOf(".") == -1) {
                f.setExtension("");
            } else {
                f.setExtension(filename.substring(filename.lastIndexOf(".") + 1));
            }
        }
        fileService.createFile(f);
        return ResponseEntity.ok("File was uploaded successfully");

    }

    @GetMapping("/download")
    public ResponseEntity<Resource> getFile(Principal principal, @RequestParam String directory,
            @RequestParam String filename) {
        if (directory == null) {
            directory = "";
        }
        String path = System.getProperty("user.dir") + "/filedir/" + principal.getName() + directory;
        Resource resource = new FileSystemResource(path + "/" + filename);

        if (resource.exists()) {
            return ResponseEntity.ok().body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/newfolder")
    public ResponseEntity<String> createFolder(Principal principal, @RequestBody FileInfo folder) {
        String directory = folder.getDirectory();
        if (directory == null) {
            directory = "";
        }
        String foldername = folder.getFilename();
        if (foldername == null) {
            return ResponseEntity.badRequest().body("No folder name provided");
        }
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName(), directory, foldername);
        if (Files.exists(path)) {
            return ResponseEntity.badRequest().body("Folder already exists");
        }
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to create folder");
        }
        folder.setOwner(principal.getName());
        folder.setFilepath(path.toString());
        folder.setExtension("folder");
        fileService.createFile(folder);
        return ResponseEntity.ok("Folder was created successfully");
    }
}
