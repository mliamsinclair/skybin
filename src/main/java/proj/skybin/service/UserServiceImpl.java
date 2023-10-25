package proj.skybin.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import proj.skybin.repository.UserRepository;
import proj.skybin.model.User;
import proj.skybin.model.Login;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User createUser(User u) {
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        userRepository.save(u);
        return u;
    }

    @Override
    public String login(Login login) {
        User user = userRepository.findByEmail(login.getEmail());
        if (user != null) {
            String encryptedPass = user.getPassword();
            boolean passwordMatch = passwordEncoder.matches(login.getPassword(), user.getPassword());
            if (passwordMatch) {
                Optional<User> userOptional = userRepository.findByEmailAndPassword(login.getEmail(),
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
