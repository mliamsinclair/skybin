package proj.skybin.controller;

import proj.skybin.model.FileInfo;
import proj.skybin.model.FolderInfo;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import proj.skybin.service.FileService;
import proj.skybin.service.FolderService;

// test controller to server login.html and home.html
// change to @RestController to test file and folder creation
@Controller
public class TestPageController {

    @Autowired
    private FileService fileService;

    @Autowired
    private FolderService folderService;

    // server login.html
    @GetMapping("/test/login")
    public String loginPage() {
        return "login";
    }
    // server home.html
    @GetMapping("/test/filetest")
    public String indexPage() {
        return "home";
    }

    // server index.html
    @GetMapping("/index")
    public String index() {
        return "index";
    }

    // create test file
    @PostMapping("/test/createfile")
    public FileInfo createFile(@RequestBody FileInfo fileInfo) {
        return fileService.createFile(fileInfo);
    }

    // create test folder
    @PostMapping("/test/createfolder")
    public FolderInfo createFolder(@RequestBody FolderInfo folderInfo) {
        return folderService.createFolder(folderInfo);
    }

    // get test file
    @GetMapping("/test/getfile")
    public Optional<FileInfo> getFile(@RequestParam String filepath) {
        return fileService.getFile(filepath);
    }

    // get test folder
    @GetMapping("/test/getfolder")
    public FolderInfo getFolder(@RequestParam String folderpath) {
        return folderService.getFolder(folderpath);
    }
}
