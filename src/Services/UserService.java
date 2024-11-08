package Services;

import Model.User;

//TODO: Also add services
//https://softwareengineering.stackexchange.com/questions/230307/mvc-what-is-the-difference-between-a-model-and-a-service

public class UserService {
    private static UserService instance;
    private User currentUser;

    private UserService() {
        currentUser = new User();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
