package Controller;

import java.io.IOException;

import javax.naming.AuthenticationException;

import Services.UserService;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    private Button loginButton;
    @FXML
    private Label lblError;

    private static final String LOGIN_SUCCESS = "Login successful";
    private static final String LOGIN_FAILED = "Login failed: ";
    private static final String LOAD_FAILED = "Failed to load main page";
    private UserService userService;
    private static final Logger LOGGER = Logger.getLogger(LoginController.class.getName());

	public LoginController() {
        this.userService = UserService.getInstance();
    }

    public void login() {
        String email = txtFieldEmail.getText();
        String password = txtFieldPassword.getText();

        try {
            this.userService.login(email, password);
            lblError.setText(LOGIN_SUCCESS);
            displayMainPage();
        } catch (AuthenticationException e) {
            handleLoginFailure(e.getMessage());
        } catch (Exception e) {
            handleLoginFailure(e.getMessage());
        }
    }

    private void handleLoginFailure(String message) {
        txtFieldEmail.clear();
        txtFieldPassword.clear();
        lblError.setText(LOGIN_FAILED + message);
    }

    private void displayMainPage() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/View/MainPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load main page", e);
            lblError.setText(LOAD_FAILED);
        }
    }
}