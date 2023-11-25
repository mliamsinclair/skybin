package proj.skybin.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import proj.skybin.repository.UserRepository;
import proj.skybin.model.UserInfo;
import proj.skybin.model.AuthRequest;
import proj.skybin.model.FolderInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private FolderService folderService;

    @Override
    public UserInfo createUser(UserInfo u) {
        if (userRepository.findByUsername(u.getUsername()).isPresent()) {
            return null;
        }
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        userRepository.save(u);
        try {
            Files.createDirectory(Paths.get(System.getProperty("user.dir"), "filedir", u.getUsername()));
            FolderInfo f = new FolderInfo();
            f.setFolderpath(Paths.get(System.getProperty("user.dir"), "filedir", u.getUsername()).toString());
            f.setOwner(u.getUsername());
            f.setDirectory("root");
            f.setFoldername(u.getUsername());
            folderService.createFolder(f);

        } catch (IOException e) {
            System.out.println("Directory already exists");
        }
        return u;
    }

    @Override
    public String login(AuthRequest login) {
        Optional<UserInfo> oUser = userRepository.findByUsername(login.getUsername());
        if (oUser.isPresent()) {
            UserInfo user = oUser.get();
            String encryptedPass = user.getPassword();
            boolean passwordMatch = passwordEncoder.matches(login.getPassword(), user.getPassword());
            if (passwordMatch) {
                Optional<UserInfo> userOptional = userRepository.findByUsernameAndPassword(login.getUsername(),
                        encryptedPass);
                if (userOptional.isPresent()) {
                    return "Login successful";
                } else {
                    return "Login failed";
                }
            } else {
                return "Passwords do not match";
            }
        } else {
            return "Email not found";
        }
    }

    @Override
    public Boolean deleteUser(String username) {
        Optional<UserInfo> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            userRepository.delete(user.get());
            return true;
        } else {
            return false;
        }
    }
}
