package proj.skybin.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.nio.file.Paths;

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
            throw new UsernameAlreadyExists("User already exists");
        }
        return ResponseEntity.ok("User created");
    }

    // authenticate a user and return a token
    // tokens are valid for two hours
    @PostMapping("/authenticate")
    public String authenticateAndGetToken(@RequestBody AuthRequest authRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword()));
        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(authRequest.getUsername());
        } else {
            throw new UsernameNotFoundException("User does not exist!");
        }
    }

    // refresh a token
    // tokens are valid for two hours
    @PostMapping("/refresh")
    public String refreshToken(Principal principal) {
        return jwtService.generateToken(principal.getName());
    }

    // delete a user
    // this will delete all files and folders associated with the user
    // this will also delete the user's account
    // this will also invalidate the user's token
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteUser(Principal principal) {
        if (userService.deleteUser(principal.getName())) {
            // delete user's directory
            Path path = Paths.get(System.getProperty("user.dir"), "filedir", principal.getName());
            if (path.toFile().delete()) {
                return ResponseEntity.ok("User deleted");
            } else {
                return ResponseEntity.badRequest().body("User directory not deleted");
            }
        } else {
            return ResponseEntity.badRequest().body("User not found");
        }
    }

    // exception for when a user already exists
    public class UsernameAlreadyExists extends RuntimeException {
        public UsernameAlreadyExists(String message) {
            super(message);
        }
    }

    @ExceptionHandler(UsernameAlreadyExists.class)
    public ResponseEntity<String> handleUsernameAlreadyExists(UsernameAlreadyExists e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
