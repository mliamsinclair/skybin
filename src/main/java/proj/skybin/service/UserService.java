package proj.skybin.service;

import proj.skybin.model.Login;
import proj.skybin.model.User;

public interface UserService {
    User createUser(User user);
    String login(Login login);
}
