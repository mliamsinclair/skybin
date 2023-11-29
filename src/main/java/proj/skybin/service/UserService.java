package proj.skybin.service;

import proj.skybin.model.AuthRequest;
import proj.skybin.model.UserInfo;

public interface UserService {
    UserInfo createUser(UserInfo user);
    String login(AuthRequest login);
    Boolean deleteUser(String username);
    Boolean updateUsername(String username, String newUsername);
    Boolean lock(String username);
    Boolean unlock(String username);
    Boolean userExists(String username);
}
