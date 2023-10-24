package proj.skybin.service;

import proj.skybin.model.User;

public interface UserService {

    public User getUser(Long id);

    public User createUser(User user);

    public User updateUser(User user);

    public void deleteUser(Long id);

    public User getUserByUsername(String username);

    public User getUserByEmail(String email);

    public User getUserByUsernameAndPassword(String username, String password);

}
