package proj.skybin.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import proj.skybin.model.AuthRequest;
import proj.skybin.model.UserInfo;
import proj.skybin.service.JwtService;
import proj.skybin.service.UserService;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // create a new user
    // this will create a new directory for the user
    // this will also create a new account for the user
    // the user will be able to log in after this
    // the home directory will be empty with the directory name being the user's
    // username
    // within the database the home directory's directory will be 'root'
    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody UserInfo user) {
        UserInfo u = userService.createUser(user);
        if (u == null) {
            return ResponseEntity.badRequest().body("User already exists");
        }
        return ResponseEntity.ok("User created");
    }

    // authenticate a user and return a token
    // tokens are valid for two hours
    @PostMapping("/authenticate")
    public ResponseEntity<String> authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
            if (authentication.isAuthenticated()) {
                return ResponseEntity.ok(jwtService.generateToken(authRequest.getUsername()));
            } else {
                return ResponseEntity.badRequest().body("Authentication failed");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Incorrect username or password");
        }
    }

    // refresh a token
    // tokens are valid for two hours
    @GetMapping("/refresh")
    public String refreshToken(Principal principal) {
        return jwtService.generateToken(principal.getName());
    }

    // delete a user
    // this will delete all files and folders associated with the user
    // this will also delete the user's account
    // this will also invalidate the user's token
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(Principal principal) {
        userService.lock(principal.getName());
        if (userService.deleteUser(principal.getName())) {
            // delete user's directory
            Path path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName());
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
                    userService.unlock(principal.getName());
                    return ResponseEntity.ok("user deleted");
                } catch (IOException e) {
                    userService.unlock(principal.getName());
                    return ResponseEntity.badRequest().body("Failed to delete user");
                }
            }
        }
        userService.unlock(principal.getName());
        return ResponseEntity.badRequest().body("User not found");
    }

    // update username
    // this will update the user's username
    // this will also update the user's directory name
    @PutMapping("/update/username")
    public ResponseEntity<String> updateUsername(Principal principal, @RequestBody String newUsername) {
        userService.lock(principal.getName());
        if (userService.updateUsername(principal.getName(), newUsername)) {
            // rename user's directory
            Path path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName());
            Path newPath = Paths.get(System.getProperty("user.dir"), "filedir", newUsername);
            // check if the folder exists
            if (Files.exists(path)) {
                // rename the folder
                try {
                    Files.move(path, newPath);
                    userService.unlock(principal.getName());
                    return ResponseEntity.ok("username updated");
                } catch (IOException e) {
                    userService.unlock(principal.getName());
                    return ResponseEntity.badRequest().body("Failed to update username");
                }
            }
        }
        userService.unlock(principal.getName());
        return ResponseEntity.badRequest().body("User not found");
    }
}