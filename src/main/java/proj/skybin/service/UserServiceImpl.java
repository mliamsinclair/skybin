package proj.skybin.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import proj.skybin.repository.UserRepository;
import proj.skybin.model.UserInfo;
import proj.skybin.model.AuthRequest;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserInfo createUser(UserInfo u) {
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        userRepository.save(u);
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
}
