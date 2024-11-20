package Services;

import java.util.ArrayList;

import javax.naming.AuthenticationException;

import Model.User;
import Model.Accounts.Account;
import at.favre.lib.crypto.bcrypt.BCrypt;

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
        User user = this.database.getUser(email);

        if (user == null) {
            throw new AuthenticationException("User not found");
        }

        if (!BCrypt.verifyer().verify(password.toCharArray(), user.getHashedPassword()).verified) {
            throw new AuthenticationException("Invalid password");
        }

        boolean isAdmin = database.getIsAdmin(user);
        user.setIsAdmin(isAdmin);

        this.currentUser = user;
    }

    public User getUser(int id) {
        return this.database.getUser(id);
    }

    public User createUser(String email, String name, String surname, String password) {
        String hashedPass = BCrypt.withDefaults().hashToString(12, password.toCharArray());
        User newUser = new User(-1, email, name, surname, hashedPass, false);
        return this.database.addUser(newUser);
    }

    public boolean checkIsAdmin(String adminPassword) {
        String hashedAdminPassword = database.getAdminPassword(currentUser.getUserID());

        if (hashedAdminPassword == null) {
            return false;
        }

        return BCrypt.verifyer().verify(adminPassword.toCharArray(), hashedAdminPassword).verified;
    }

    public User setAdmin(User user, boolean isAdmin, String password) {
        user.setIsAdmin(isAdmin);

        if (isAdmin) {
            String hashedPass = BCrypt.withDefaults().hashToString(12, password.toCharArray());
            database.addAdmin(user.getUserID(), hashedPass);
        } else {
            database.removeAdmin(user.getUserID());
        }

        return user;
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
