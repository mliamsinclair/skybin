package proj.skybin.service;

import proj.skybin.model.AuthRequest;
import proj.skybin.model.UserInfo;

public interface UserService {
    UserInfo createUser(UserInfo user);
    String login(AuthRequest login);
}
