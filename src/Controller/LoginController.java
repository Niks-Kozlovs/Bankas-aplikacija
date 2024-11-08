package Controller;

import java.io.IOException;
import java.sql.ResultSet;

import javax.naming.AuthenticationException;

import Model.User;
import Model.Accounts.Konti;
import Services.Database;
import Services.UserService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {
    @FXML
    private TextField txtFieldEmail;
    @FXML
    private PasswordField txtFieldPassword;
    @FXML
    private Button login_button;
    @FXML
    private Label lblError;

    private static final String LOGIN_SUCCESS = "Login successful";
    private static final String LOGIN_FAILED = "Login failed: ";
	private static final String LOAD_FAILED = "Failed to load main page.";

	private Database database;
    private UserService userService;

	public LoginController() {
        this.database = Database.getInstance();
        this.userService = UserService.getInstance();
    }

    public void login() {
        String email = txtFieldEmail.getText();
        String password = txtFieldPassword.getText();

        try {
			//TODO: This should be moved to user service instead of right from db.
            User user = this.database.login(email, password);
            initializeUserSession(user);
            displayMainPage();
        } catch (AuthenticationException e) {
            loginFail(e.getMessage());
        }
    }

	//TODO: Refactor this to be in UserService
    private void initializeUserSession(User user) {
        Konti konti = new Konti();
		ResultSet accResultSet = this.database.getAccounts(user.getUserID());
        konti.addAccounts(accResultSet);
        user.setAccounts(konti);

        this.userService.setCurrentUser(user);
        lblError.setText(LOGIN_SUCCESS);
    }

    private void loginFail(String message) {
        txtFieldEmail.clear();
        txtFieldPassword.clear();
        lblError.setText(LOGIN_FAILED + message);
    }

    private void displayMainPage() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/View/MainPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) login_button.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            lblError.setText(LOAD_FAILED);
        }
    }
}