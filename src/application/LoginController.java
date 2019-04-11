package application;

import java.io.IOException;
import java.util.Optional;

import Accounts.Konti;
import Database.Database;
import User.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Stage;

public class LoginController {
	@FXML
	private TextField txtFieldEmail;
	@FXML
	private PasswordField txtFieldPassword;
	@FXML
	private Button login_button;
	@FXML
	Label lblError; // Shows status of login (error loging in/success/wront email/pass)

	public void login() {
		String email;
		String password;
		email = txtFieldEmail.getText();
		password = txtFieldPassword.getText();

		User user = new User();
		Database db = new Database();

		if (db.login(email, password, user)) {
			lblError.setText("Login succesful");

			Konti konti = new Konti();
			konti.addAccounts(db.getAccounts(user.getUserID()));
			user.setAccounts(konti);

			Stage stage = (Stage) login_button.getScene().getWindow();
			stage.close();

			if (user.getIsAdmin()) {
				askIfWantAdminPanel(user, db);
			}
			openMainForm(user, db);

		} else {
			loginFail();
		}
	}

	private void loginFail() {
		txtFieldEmail.clear();
		txtFieldPassword.clear();
		lblError.setText("Login failed");
	}

	private void openMainForm(User user, Database db) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("MainPage.fxml"));
			Parent root = (Parent) loader.load();

			MainFormController controller = loader.getController();
			controller.setUp(user, db);
			Stage stage = new Stage();
			stage.setOnHidden(e -> controller.shutdown());
			stage.setResizable(false);

			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void askIfWantAdminPanel(User user, Database db) {
		boolean loggedIn = false;
		boolean pressedCancel = false;
		// Asks if user wants to log in with admin
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Admin");
		alert.setHeaderText("Do you want to open admin window?");
		alert.setContentText("You will need an admin window to access it");
		Optional<ButtonType> result = alert.showAndWait();

		if (result.get() == ButtonType.OK) {
			while (!loggedIn && !pressedCancel) {
				TextInputDialog dialog = new TextInputDialog("");
				dialog.setTitle("Admin login");
				dialog.setHeaderText("Please enter admin pass");
				dialog.setContentText("Password:");
				// from https://code.makery.ch/blog/javafx-dialogs-official/

				Optional<String> result1 = dialog.showAndWait();
				if (result1.isPresent()) {
					if (db.loginAdmin(user.getUserID(), result1.get())) {
						loggedIn = true;
						openAdminWindow(user, db);
					}
				} else {
					pressedCancel = true;
				}
			}

		}

	}

	private void openAdminWindow(User user, Database db) {
		try {
			FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminPage.fxml"));
			Parent root = (Parent) loader.load();

			AdminPageController controller = loader.getController();
			controller.setUp(user, db);
			Stage stage = new Stage();
			stage.setResizable(false);

			stage.setScene(new Scene(root));
			stage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
