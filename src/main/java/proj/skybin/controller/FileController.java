package proj.skybin.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import proj.skybin.model.FileInfo;
import proj.skybin.model.FileNode;
import proj.skybin.model.FolderInfo;
import proj.skybin.service.FileService;
import proj.skybin.service.FolderService;

@RestController
@RequestMapping("/api/user")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FolderService folderservice;

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
        if (directory == null || directory.equals("null") || directory.equals("")) {
            directory = "/";
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
        f.setIsDirectory(false);
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
    public ResponseEntity<String> createFolder(Principal principal, @RequestBody FolderInfo folder) {
        String directory = folder.getDirectory();
        if (directory == null) {
            directory = "/";
        }
        String foldername = folder.getFoldername();
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
        String folderpath = path.toString();
        folder.setFolderpath(folderpath);
        folder.setOwner(principal.getName());
        folder.setDirectory(directory);
        // add the folder to the database
        folderservice.createFolder(folder);
        return ResponseEntity.ok("Folder was created successfully");
    }

    // get all folders in the user's directory
    // uses the provided token to get the username
    // list of files is returned in the 'files' array of the FolderInfo object
    @GetMapping("/folders")
    public ResponseEntity<List<FolderInfo>> getHomeDirectoryContents(Principal principal) {
        String owner = principal.getName();
        List<FolderInfo> home = folderservice.getHomeDirectoryContents(owner);
        return ResponseEntity.ok(home);
    }

    // list all files and folders of a user
    @GetMapping("/listFiles")
    public ResponseEntity<FileNode> listFiles(Principal principal) {
        FileNode root;
        try {
            Path path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName());
            root = new FileNode(path.toFile());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Failed to list files", e);
        }
        return ResponseEntity.ok(root);
    }

    // get all files from a folder
    // if the directory is null, the user's home directory is used
    // uses the provided token to get the username
    @GetMapping("/folderFiles")
    public ResponseEntity<List<FileInfo>> getFolderContents(Principal principal, @RequestParam String directory) {
        if (directory == null || directory.equals("") || directory.equals("/")) {
            List<FileInfo> files = fileService.getAllFiles(principal.getName());
            return ResponseEntity.ok(files);
        }
        String owner = principal.getName();
        List<FileInfo> files = fileService.getDirectoryContents(directory, owner);
        return ResponseEntity.ok(files);
    }

    // get all files and folders in the user's directory
    // uses the provided token to get the username
    // the folders are added to the files list and treated as files
    @GetMapping("/all")
    public ResponseEntity<List<FileInfo>> getAllDirectoryContents(Principal principal) {
        String owner = principal.getName();
        List<FileInfo> files = fileService.getAllFiles(owner);
        List<FolderInfo> folders = folderservice.getHomeDirectoryContents(owner);
        // add the folders to the files list
        for (FolderInfo f : folders) {
            FileInfo folder = new FileInfo();
            folder.setFilename(f.getFoldername());
            folder.setDirectory(f.getDirectory());
            folder.setFilepath(f.getFolderpath());
            folder.setOwner(f.getOwner());
            folder.setUploadDate(f.getUploadDate());
            folder.setIsDirectory(true);
            files.add(folder);
        }
        return ResponseEntity.ok(files);
    }

    // delete a file from the server
    // uses the provided token to get the username
    @DeleteMapping("/deleteFile")
    public ResponseEntity<String> deleteFile(Principal principal, @RequestBody FileInfo file) {
        String owner = principal.getName();
        String directory = file.getDirectory();
        String filename = file.getFilename();
        if (directory == null) {
            directory = "";
        }
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", owner, directory, filename);
        // check if the file exists
        if (Files.exists(path)) {
            // delete the file
            try {
                Files.delete(path);
            } catch (IOException e) {
                return ResponseEntity.badRequest().body("Failed to delete file");
            }
            // delete the file from the database
            fileService.deleteFile(path.toString());
            return ResponseEntity.ok("File was deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("File does not exist");
        }
    }

    // delete a folder and all of its contents
    // uses the provided token to get the username
    @DeleteMapping("/deleteFolder")
    public ResponseEntity<String> deleteFolder(Principal principal, @RequestBody FolderInfo folder) {
        String owner = principal.getName();
        String directory = folder.getDirectory();
        String foldername = folder.getFoldername();
        if (directory == null) {
            directory = "";
        }
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", owner, directory, foldername);
        // check if the folder exists
        if (Files.exists(path)) {
            // delete the folder
            try {
                Files.delete(path);
            } catch (IOException e) {
                return ResponseEntity.badRequest().body("Failed to delete folder");
            }
            // delete the folder from the database
            folderservice.deleteFolder(path.toString());
            return ResponseEntity.ok("Folder was deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Folder does not exist");
        }
    }

    // share a file with another user
    // uses the provided token to get the username
    @PostMapping("/share")
    public ResponseEntity<String> shareFile(Principal principal, @RequestBody FileInfo file,
            @RequestParam String user) {
        String owner = principal.getName();
        String directory = file.getDirectory();
        String filename = file.getFilename();
        if (directory == null) {
            directory = "";
        }
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", owner, directory, filename);
        // check if the file exists
        if (Files.exists(path)) {
            // check if the user exists
            Path userPath = Paths.get(System.getProperty("user.dir"), "filedir", user);
            if (Files.exists(userPath)) {
                // check if the user already has access to the file
                FileInfo f = fileService.getFile(path.toString()).get();
                String[] sharedUsers = f.getSharedUsers();
                if (sharedUsers != null) {
                    for (String s : sharedUsers) {
                        if (s.equals(user)) {
                            return ResponseEntity.badRequest().body("User already has access to this file");
                        }
                    }
                }
                // add the user to the list of users with access to the file
                if (sharedUsers == null) {
                    sharedUsers = new String[1];
                    sharedUsers[0] = user;
                } else {
                    String[] newUsers = new String[sharedUsers.length + 1];
                    for (int i = 0; i < sharedUsers.length; i++) {
                        newUsers[i] = sharedUsers[i];
                    }
                    newUsers[sharedUsers.length] = user;
                    sharedUsers = newUsers;
                }
                f.setSharedUsers(sharedUsers);
                fileService.createFile(f);
                return ResponseEntity.ok("File was shared successfully");
            } else {
                return ResponseEntity.badRequest().body("User does not exist");
            }
        } else {
            return ResponseEntity.badRequest().body("File does not exist");
        }
    }

    // unshare a file with another user
    // uses the provided token to get the username
    @PostMapping("/unshare")
    public ResponseEntity<String> unshareFile(Principal principal, @RequestBody FileInfo file,
            @RequestParam String user) {
        String owner = principal.getName();
        String directory = file.getDirectory();
        String filename = file.getFilename();
        if (directory == null) {
            directory = "";
        }
        Path path = Paths.get(System.getProperty("user.dir"), "filedir", owner, directory, filename);
        // check if the file exists
        if (Files.exists(path)) {
            // check if the user exists
            Path userPath = Paths.get(System.getProperty("user.dir"), "filedir", user);
            if (Files.exists(userPath)) {
                // check if the user already has access to the file
                FileInfo f = fileService.getFile(path.toString()).get();
                String[] sharedUsers = f.getSharedUsers();
                if (sharedUsers == null) {
                    return ResponseEntity.badRequest().body("User does not have access to this file");
                } else {
                    boolean found = false;
                    for (int i = 0; i < sharedUsers.length; i++) {
                        if (sharedUsers[i].equals(user)) {
                            found = true;
                            String[] newUsers = new String[sharedUsers.length - 1];
                            for (int j = 0; j < i; j++) {
                                newUsers[j] = sharedUsers[j];
                            }
                            for (int j = i + 1; j < sharedUsers.length; j++) {
                                newUsers[j - 1] = sharedUsers[j];
                            }
                            sharedUsers = newUsers;
                            break;
                        }
                    }
                    if (!found) {
                        return ResponseEntity.badRequest().body("User does not have access to this file");
                    }
                }
                f.setSharedUsers(sharedUsers);
                fileService.createFile(f);
                return ResponseEntity.ok("File was unshared successfully");
            } else {
                return ResponseEntity.badRequest().body("User does not exist");
            }
        } else {
            return ResponseEntity.badRequest().body("File does not exist");
        }
    }

    // get all files that have been shared with the user
    // uses the provided token to get the username
    @GetMapping("/shared")
    public ResponseEntity<List<FileInfo>> getSharedFiles(Principal principal) {
        String owner = principal.getName();
        List<FileInfo> files = fileService.getAllFiles(owner);
        // remove all files that have not been shared with the user
        for (int i = 0; i < files.size(); i++) {
            FileInfo f = files.get(i);
            String[] sharedUsers = f.getSharedUsers();
            if (sharedUsers == null) {
                files.remove(i);
                i--;
            } else {
                boolean found = false;
                for (String s : sharedUsers) {
                    if (s.equals(owner)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    files.remove(i);
                    i--;
                }
            }
        }
        return ResponseEntity.ok(files);
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
