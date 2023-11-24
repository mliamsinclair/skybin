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

    // upload a file to the server
    // the file is stored in the fildir folder within the user's home folder
    // and within the directory specified in the request
    // the file information is automatically added to the database
    // uses the provided token to get the username
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(Principal principal, @RequestParam String directory,
            @RequestParam MultipartFile file) throws IOException {
        // check if the directory is null/empty/invalid
        if (directory == null || directory.equals("null") || directory.equals("/")) {
            directory = "";
        }
        // check if the file is empty
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }
        // check if the file already exists
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName(), directory,
                file.getOriginalFilename());
        if (Files.exists(path)) {
            return ResponseEntity.badRequest().body("File already exists");
        }
        // recieve the file and save it to the database
        Files.copy(file.getInputStream(), path);
        FileInfo f = new FileInfo();
        f.setOwner(principal.getName());
        String filename = file.getOriginalFilename();
        f.setFilename(filename);
        f.setDirectory(directory);
        f.setFilepath(path.toString());
        // set the file extension
        if (filename != null) {
            if (filename.lastIndexOf(".") == -1) {
                f.setExtension("");
            } else {
                f.setExtension(filename.substring(filename.lastIndexOf(".") + 1));
            }
        }
        // add the file to the database
        fileService.createFile(f);
        return ResponseEntity.ok("File was uploaded successfully");

    }

    // download a file from the server
    // uses the provided token to get the username
    @GetMapping("/download")
    public ResponseEntity<Resource> getFile(Principal principal, @RequestParam String directory,
            @RequestParam String filename) {
        if (directory == null) {
            directory = "";
        }
        // get the file from the server
        String path = System.getProperty("user.dir") + "/filedir/" + principal.getName() + directory;
        Resource resource = new FileSystemResource(path + "/" + filename);
        // check if the file exists
        if (resource.exists()) {
            return ResponseEntity.ok().body(resource);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // create a new folder
    // the folder is created in the user's directory at the specified path
    // within the FileInfo object
    // the file information is automatically added to the database and the username
    // is extracted from the token
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
        // folder already exists
        if (Files.exists(path)) {
            return ResponseEntity.badRequest().body("Folder already exists");
        }
        // create the folder
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            return ResponseEntity.badRequest().body("Failed to create folder");
        }
        // add the folder to the database
        folder.setOwner(principal.getName());
        folder.setFilepath(path.toString());
        folder.setExtension("folder");
        fileService.createFile(folder);
        return ResponseEntity.ok("Folder was created successfully");
    }
}
