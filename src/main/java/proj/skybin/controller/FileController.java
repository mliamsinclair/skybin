package proj.skybin.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import proj.skybin.model.FileInfo;
import proj.skybin.service.FileService;

@RestController
@RequestMapping("/api/user")
public class FileController {
    
    @Autowired
    private FileService fileService;

    @PostMapping("/newfile")
    public FileInfo createFile(Principal principal, @RequestBody FileInfo f) {
        String owner = principal.getName();
        f.setOwner(owner);
        return fileService.createFile(f);
    }
}
