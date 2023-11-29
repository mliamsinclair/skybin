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
import java.nio.file.Path;
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
            f.setPath(Paths.get(System.getProperty("user.dir"), "filedir", u.getUsername()).toString());
            f.setOwner(u.getUsername());
            f.setDirectory("root");
            f.setName(u.getUsername());
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
            // delete user's directory
            folderService.deleteFolder(username);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Boolean updateUsername(String username, String newUsername) {
        Optional<UserInfo> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            user.get().setUsername(newUsername);
            userRepository.save(user.get());
            // rename user's directory
            Path path = Paths.get(System.getProperty("user.dir"), "filedir", username);
            Path newPath = Paths.get(System.getProperty("user.dir"), "filedir", newUsername);
            // check if the folder exists
            if (path.toFile().exists()) {
                // rename the folder
                try {
                    Files.move(path, newPath);
                } catch (IOException e) {
                    System.out.println("Error renaming user's directory");
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public Boolean lock(String username) {
        Optional<UserInfo> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            while (user.get().getLock()) {
                try {
                    Thread.sleep(1000);
                    user = userRepository.findByUsername(username);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            user.get().setLock(true);
            userRepository.save(user.get());
            return true;
        }
        return false;
    }

    @Override
    public Boolean unlock(String username) {
        Optional<UserInfo> user = userRepository.findByUsername(username);
        if (user.isPresent()) {
            user.get().setLock(false);
            userRepository.save(user.get());
            return true;
        }
        return false;
    }

    // check if user exists
    public Boolean userExists(String username) {
        Optional<UserInfo> user = userRepository.findByUsername(username);
        return user.isPresent();
    }
}
