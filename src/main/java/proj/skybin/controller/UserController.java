package proj.skybin.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
