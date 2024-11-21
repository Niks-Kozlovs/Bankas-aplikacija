package Controller;

import java.io.IOException;

import javax.naming.AuthenticationException;

import Services.UserService;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
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

        Platform.runLater(() -> {
            Stage stage = (Stage) lblError.getScene().getWindow();
            stage.setTitle("Log in");
            //Add image to the window
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/res/Logo.jpg")));
        });

    }

    public void login() {
        String email = txtFieldEmail.getText();
        String password = txtFieldPassword.getText();

        if (email.isEmpty() || password.isEmpty()) {
            lblError.setText("Please fill in all fields");
            return;
        }

        try {
            this.userService.login(email, password);
        } catch (AuthenticationException e) {
            handleLoginFailure(e.getMessage());
        } catch (Exception e) {
            handleLoginFailure(e.getMessage());
        }

        lblError.setText(LOGIN_SUCCESS);
        displayMainPage();
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
            Stage stage = (Stage) lblError.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to load main page", e);
            lblError.setText(LOAD_FAILED);
        }
    }
}