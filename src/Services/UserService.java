package Services;

import java.util.ArrayList;

import javax.naming.AuthenticationException;

import Model.User;
import Model.Accounts.Account;

public class UserService {
    private static UserService instance;
    private Database database;
    private User currentUser;

    private UserService() {
        this.database = Database.getInstance();
    }

    public static UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public void login(String email, String password) throws AuthenticationException {
        this.currentUser = this.database.login(email, password);
    }

    public ArrayList<Account> getUserAccounts() {
        return this.database.getAccounts(currentUser.getUserID());
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }
}
