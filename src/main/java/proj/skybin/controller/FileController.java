package proj.skybin.controller;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.Principal;
import java.util.List;
import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import proj.skybin.model.DirectoryWrapper;
import proj.skybin.model.FileInfo;
import proj.skybin.model.FileNode;
import proj.skybin.model.FolderInfo;
import proj.skybin.service.FileService;
import proj.skybin.service.FolderService;
import proj.skybin.service.UserService;

@RestController
@RequestMapping("/api/user")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FolderService folderservice;

    @Autowired
    private UserService userService;

    // for testing the addition of new files to the database, can also be used to
    // create a newfile without an upload (?)
    @PostMapping("/newfile")
    public FileInfo createFile(Principal principal, @RequestBody FileInfo f) {
        userService.lock(principal.getName());
        String owner = principal.getName();
        f.setOwner(owner);
        userService.unlock(principal.getName());
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
        Instant start = Instant.now();
        userService.lock(principal.getName());
        // check if the directory is null/empty/invalid
        if (directory == null || directory.equals("null") || directory.equals("") || directory.equals("\\")) {
            directory = "/";
        }
        // check if the file is empty
        if (file.isEmpty()) {
            userService.unlock(principal.getName());
            return ResponseEntity.badRequest().body("File is empty");
        }
        // check if the file already exists
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName(), directory,
                file.getOriginalFilename());
        System.out.println(path.toString());
        if (Files.exists(path)) {
            // add a number to the end of the filename
            int i = 1;
            for (; i < 100; i++) {
                path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName(), directory,
                        file.getOriginalFilename() + "(" + i + ")");
                if (!Files.exists(path)) {
                    break;
                }
            }
            // change the name of the file
            String filename = file.getOriginalFilename() + "(" + i + ")";
            path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName(), directory, filename);
        }
        // recieve the file and save it to the database
        try {
            Files.copy(file.getInputStream(), path);
        } catch (IOException e) {
            try {
                // replace all back slashes with forward slashes
                String newPath = path.toString().replace("\\", "/");
                path = Paths.get(newPath);
                Files.copy(file.getInputStream(), path);
            } catch (IOException e2) {
                userService.unlock(principal.getName());
                return ResponseEntity.badRequest().body("Failed to upload file");
            }
        }
        FileInfo f = new FileInfo();
        f.setOwner(principal.getName());
        String filename = file.getName();
        f.setName(filename);
        f.setDirectory(directory);
        f.setPath(path.toString());
        f.setIsDirectory(false);
        // add the file to the database
        fileService.createFile(f);
        userService.unlock(principal.getName());
        Instant end = Instant.now();
        System.out.println("Time to upload: " + (end.toEpochMilli() - start.toEpochMilli()) + "ms");
        return ResponseEntity.ok("File was uploaded successfully");

    }

    // download a file from the server
    // uses the provided token to get the username
    @GetMapping("/download")
    public ResponseEntity<Resource> getFile(Principal principal, @RequestParam String directory,
            @RequestParam String filename) throws IOException {
        // save current timestamp
        Instant start = Instant.now();
        userService.lock(principal.getName());
        if (directory == null || directory.equals("null") || directory.equals("") || directory.equals("\\")) {
            directory = "/";
        }
        // get the file from the server
        String path = System.getProperty("user.dir") + "/filedir/" + principal.getName() + directory;
        Resource resource = new FileSystemResource(path + "/" + filename);
        // check if the file exists
        if (resource.exists()) {
            String mimeType = Files.probeContentType(Paths.get(path + "/" + filename));
            userService.unlock(principal.getName());
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(mimeType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } else {
            // replace all back slashes with forward slashes
            path = path.replace("\\", "/");
            resource = new FileSystemResource(path + "/" + filename);
            if (resource.exists()) {
                String mimeType = Files.probeContentType(Paths.get(path + "/" + filename));
                userService.unlock(principal.getName());
                Instant end = Instant.now();
                System.out.println("Time to download: " + (end.toEpochMilli() - start.toEpochMilli()) + "ms");
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(mimeType))
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                userService.unlock(principal.getName());
                return ResponseEntity.badRequest().body(null);
            }
        }
    }

    // create a new folder
    // the folder is created in the user's directory at the specified path
    // within the FileInfo object
    // the file information is automatically added to the database and the username
    // is extracted from the token
    @PostMapping("/newfolder")
    public ResponseEntity<String> createFolder(Principal principal, @RequestBody FolderInfo folder) {
        userService.lock(principal.getName());
        String directory = folder.getDirectory();
        if (directory == null || directory.equals("null") || directory.equals("") || directory.equals("\\")) {
            directory = "/";
        }
        String foldername = folder.getName();
        if (foldername == null) {
            userService.unlock(principal.getName());
            return ResponseEntity.badRequest().body("No folder name provided");
        }
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName(), directory, foldername);
        // folder already exists
        if (Files.exists(path)) {
            userService.unlock(principal.getName());
            return ResponseEntity.badRequest().body("Folder already exists");
        }
        // create the folder
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            // replace all back slashes with forward slashes
            String newPath = path.toString().replace("\\", "/");
            path = Paths.get(newPath);
            try {
                Files.createDirectory(path);
            } catch (IOException e2) {
                userService.unlock(principal.getName());
                return ResponseEntity.badRequest().body("Failed to create folder");
            }
        }
        String folderpath = path.toString();
        folder.setPath(folderpath);
        folder.setOwner(principal.getName());
        folder.setDirectory(directory);
        // add the folder to the database
        folderservice.createFolder(folder);
        userService.unlock(principal.getName());
        return ResponseEntity.ok("Folder was created successfully");
    }

    // get all folders in the user's directory
    // uses the provided token to get the username
    // list of files is returned in the 'files' array of the FolderInfo object
    @GetMapping("/folders")
    public ResponseEntity<List<FolderInfo>> getHomeDirectoryContents(Principal principal) {
        userService.lock(principal.getName());
        String owner = principal.getName();
        List<FolderInfo> home = folderservice.getHomeDirectoryContents(owner);
        userService.unlock(principal.getName());
        return ResponseEntity.ok(home);
    }

    // list all files and folders of a user
    // using data from the database
    @GetMapping("/home")
    public ResponseEntity<FolderInfo> getHomeDirectory(Principal principal) {
        userService.lock(principal.getName());
        String owner = principal.getName();
        FolderInfo home = folderservice.getFolder(owner, "root", owner);
        if (home == null) {
            userService.unlock(principal.getName());
            return ResponseEntity.badRequest().body(home);
        }
        userService.unlock(principal.getName());
        return ResponseEntity.ok(home);
    }

    // list all files and folders of a user wrapped using the DirectoryWrapper class
    @GetMapping("/homeWrapped")
    public ResponseEntity<DirectoryWrapper> getHomeDirectoryWrapped(Principal principal) {
        userService.lock(principal.getName());
        String owner = principal.getName();
        FolderInfo home = folderservice.getFolderByPath(owner);
        if (home == null) {
            userService.unlock(principal.getName());
            return ResponseEntity.badRequest().body(null);
        }
        DirectoryWrapper homeWrapped = new DirectoryWrapper(home);
        userService.unlock(principal.getName());
        return ResponseEntity.ok(homeWrapped);
    }

    // list all files and folders of a user
    @GetMapping("/listFiles")
    public ResponseEntity<FileNode> listFiles(Principal principal) {
        userService.lock(principal.getName());
        FileNode root;
        try {
            Path path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName());
            root = new FileNode(path.toFile());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to list files", e);
        }
        userService.unlock(principal.getName());
        return ResponseEntity.ok(root);
    }

    // get all files from a folder
    // if the directory is null, the user's home directory is used
    // uses the provided token to get the username
    @GetMapping("/folderFiles")
    public ResponseEntity<List<FileInfo>> getFolderContents(Principal principal, @RequestParam String directory) {
        userService.lock(principal.getName());
        if (directory == null || directory.equals("") || directory.equals("/")) {
            List<FileInfo> files = fileService.getAllFiles(principal.getName());
            userService.unlock(principal.getName());
            return ResponseEntity.ok(files);
        }
        String owner = principal.getName();
        List<FileInfo> files = fileService.getDirectoryContents(directory, owner);
        userService.unlock(principal.getName());
        return ResponseEntity.ok(files);
    } 
    // uses the provided token to get the username
    @GetMapping("/file")
    public ResponseEntity<FileInfo> getFileInfo(Principal principal, @RequestParam String directory,
            @RequestParam String filename) {
        userService.lock(principal.getName());
        if (directory == null) {
            directory = "";
        }
        String owner = principal.getName();
        FileInfo f = fileService.getFile(owner, directory, filename);
        if (f == null) {
            userService.unlock(principal.getName());
            return ResponseEntity.badRequest().body(null);
        }
        userService.unlock(principal.getName());
        return ResponseEntity.ok(f);
    } 
    
    // uses the provided token to get the username
    // requires the directory and foldername of the folder
    @GetMapping("/folder")
    public ResponseEntity<FolderInfo> getFolderInfo(Principal principal, @RequestParam String directory,
            @RequestParam String foldername) {
        userService.lock(principal.getName());
        if (directory == null) {
            directory = "";
        }
        String owner = principal.getName();
        FolderInfo f = folderservice.getFolder(owner, directory, foldername);
        if (f == null) {
            userService.unlock(principal.getName());
            return ResponseEntity.badRequest().body(null);
        }
        userService.unlock(principal.getName());
        return ResponseEntity.ok(f);
    }

    // delete a file from the server
    // uses the provided token to get the username
    @DeleteMapping("/deleteFile")
    public ResponseEntity<String> deleteFile(Principal principal, @RequestBody FileInfo file) {
        userService.lock(principal.getName());
        String owner = principal.getName();
        String directory = file.getDirectory();
        if (directory == null || directory.equals("\\\\") || directory.equals("\\\\\\") || directory.equals("\\")) {
            directory = "";
        }
        String filename = file.getName();
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", owner, directory, filename);
        // check if the file exists
        if (Files.exists(path)) {
            // delete the file
            try {
                Files.delete(path);
            } catch (IOException e) {
                // replace all back slashes with forward slashes
                String newPath = path.toString().replace("\\", "/");
                path = Paths.get(newPath);
                try {
                    Files.delete(path);
                } catch (IOException e2) {
                    userService.unlock(principal.getName());
                    return ResponseEntity.badRequest().body("Failed to delete file");
                }
            }
            // delete the file from the database
            String pathString = path.toString();
            String[] pathArray = pathString.split(owner);
            if (pathArray.length == 1) {
                pathString = owner;
            } else {
                pathString = owner + pathArray[1];
            }
            fileService.deleteFile(pathString);
            userService.unlock(principal.getName());
            return ResponseEntity.ok("File was deleted successfully");
        } else {
            userService.unlock(principal.getName());
            return ResponseEntity.badRequest().body("File does not exist");
        }
    }

    // delete a folder and all of its contents
    // uses the provided token to get the username
    @DeleteMapping("/deleteFolder")
    public ResponseEntity<String> deleteFolder(Principal principal, @RequestBody FolderInfo folder) {
        userService.lock(principal.getName());
        String owner = principal.getName();
        String directory = folder.getDirectory();
        if (directory == null || directory.equals("\\\\") || directory.equals("\\\\\\") || directory.equals("\\")) {
            directory = "";
        }
        String foldername = folder.getName();
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", owner, directory, foldername);
        // check if the folder exists
        if (Files.exists(path)) {
            // delete the folder
            try {
                Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        Files.delete(file);
                        userService.unlock(principal.getName());
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        Files.delete(dir);
                        userService.unlock(principal.getName());
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                userService.unlock(principal.getName());
                return ResponseEntity.badRequest().body("Failed to delete folder");
            }
            // delete the folder from the database
            String pathString = path.toString();
            String[] pathArray = pathString.split(owner);
            if (pathArray.length == 1) {
                pathString = owner;
            } else {
                pathString = owner + pathArray[1];
            }
            folderservice.deleteFolder(pathString);
            userService.unlock(principal.getName());
            return ResponseEntity.ok("Folder was deleted successfully");
        } else {
            userService.unlock(principal.getName());
            return ResponseEntity.badRequest().body("Folder does not exist");
        }
    }

    // rename a file
    // uses the provided token to get the username
    @PutMapping("/renameFile")
    public ResponseEntity<String> renameFile(Principal principal, @RequestBody FileInfo file,
            @RequestParam String newFilename) {
        userService.lock(principal.getName());
        String owner = principal.getName();
        String directory = file.getDirectory();
        if (directory == null || directory.equals("\\\\") || directory.equals("\\\\\\") || directory.equals("\\")) {
            directory = "";
        }
        String filename = file.getName();
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", owner, directory, filename);
        // check if the file exists
        if (Files.exists(path)) {
            // rename the file
            try {
                Files.move(path, path.resolveSibling(newFilename));
            } catch (IOException e) {
                // replace all back slashes with forward slashes
                String newPath = path.toString().replace("\\", "/");
                path = Paths.get(newPath);
                try {
                    Files.move(path, path.resolveSibling(newFilename));
                } catch (IOException e2) {
                    userService.unlock(principal.getName());
                    return ResponseEntity.badRequest().body("Failed to rename file");
                }
            }
            // rename the file in the database
            String pathString = path.toString();
            String[] pathArray = pathString.split(owner);
            if (pathArray.length == 1) {
                pathString = owner;
            } else {
                pathString = owner + pathArray[1];
            }
            fileService.renameFile(pathString, newFilename);
            userService.unlock(principal.getName());
            return ResponseEntity.ok("File was renamed successfully");
        } else {
            userService.unlock(principal.getName());
            return ResponseEntity.badRequest().body("File does not exist");
        }
    }

    // rename a folder
    // uses the provided token to get the username
    @PutMapping("/renameFolder")
    public ResponseEntity<String> renameFolder(Principal principal, @RequestBody FolderInfo folder,
            @RequestParam String newFoldername) {
        userService.lock(principal.getName());
        String owner = principal.getName();
        String directory = folder.getDirectory();
        if (directory == null || directory.equals("\\\\") || directory.equals("\\\\\\") || directory.equals("\\")) {
            directory = "";
        }
        String foldername = folder.getName();
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", owner, directory, foldername);
        // check if the folder exists
        if (Files.exists(path)) {
            // rename the folder
            try {
                Files.move(path, path.resolveSibling(newFoldername));
            } catch (IOException e) {
                // replace all back slashes with forward slashes
                String newPath = path.toString().replace("\\", "/");
                path = Paths.get(newPath);
                try {
                    Files.move(path, path.resolveSibling(newFoldername));
                } catch (IOException e2) {
                    userService.unlock(principal.getName());
                    return ResponseEntity.badRequest().body("Failed to rename folder");
                }
            }
            // rename the folder in the database
            String pathString = path.toString();
            String[] pathArray = pathString.split(owner);
            if (pathArray.length == 1) {
                pathString = owner;
            } else {
                pathString = owner + pathArray[1];
            }
            folderservice.renameFolder(pathString, newFoldername);
            userService.unlock(principal.getName());
            return ResponseEntity.ok("Folder was renamed successfully");
        } else {
            userService.unlock(principal.getName());
            return ResponseEntity.badRequest().body("Folder does not exist");
        }
    }

    // user not found exception
    public class UserNotFoundException extends Exception {
        public UserNotFoundException(String errorMessage) {
            super(errorMessage);
        }
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
