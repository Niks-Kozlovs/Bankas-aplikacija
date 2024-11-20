package Controller;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.ResourceBundle;

import Model.User;
import Model.Accounts.Account;
import Model.Accounts.AccountType;
import Services.Database;
import Services.UserService;
import Util.InputFormatter;
import at.favre.lib.crypto.bcrypt.BCrypt;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AdminPageController implements Initializable {
	private UserService userService;
	private Database database;
	private User currSelectedUser;

	@FXML
	private TextField txtID;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtSurname;
	@FXML
	private TextField txtEmail;
	@FXML
	private TextField txtPass;
	@FXML
	private TableView<Account> tableAccounts = new TableView<>();

	@FXML
	private Button btnAddUser;
	@FXML
	private Button btnRemoveUser;
	@FXML
	private Button btnUpdate;
	@FXML
	private Button btnPassword;
	@FXML
	private Button btnDeleteAccount;
	@FXML
	private Button btnAddAlgasKonts;
	@FXML
	private Button btnAddNoguldijumaKonts;
	@FXML
	private Button btnAddKreditaKonts;

	@FXML
	private Label lblStatus;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.database = Database.getInstance();
		this.userService = UserService.getInstance();

		this.txtID.setTextFormatter(InputFormatter.getOnlyDigitsFormatter());
		this.txtEmail.setTextFormatter(InputFormatter.getEmailFormatter());

		btnDeleteAccount.setDisable(true);
		disableKontsButtons(true);

		TableColumn<Account, Integer> accountNumber = new TableColumn<Account, Integer>("Account number");
		accountNumber.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAccountNumber()).asObject());

		TableColumn<Account, BigDecimal> money = new TableColumn<Account, BigDecimal>("Balance");
		money.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBalance()));

		TableColumn<Account, String> moneyType = new TableColumn<Account, String>("Currency");
		moneyType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCurrencySymbol()));

		TableColumn<Account, String> accountType = new TableColumn<Account, String>("Account type");
		accountType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAccountName()));

		List<TableColumn<Account, ?>> columns = Arrays.asList(accountNumber, money, moneyType, accountType);
		tableAccounts.getColumns().addAll(columns);
	}

	@FXML
	public void onIdChanged() {
		if (txtID.getText().isBlank()) {
			clearFields();
			disableKontsButtons(true);
			return;
		}

		try {
			int userId = Integer.parseInt(txtID.getText());
			currSelectedUser = database.getUser(userId);
			setFields();
		} catch (NumberFormatException e) {
			clearFields();
			disableKontsButtons(true);
			lblStatus.setText("Invalid ID format");
			return;
		}

		if (currSelectedUser == null) {
			clearFields();
			disableKontsButtons(true);
			return;
		}
	}

	@FXML
	public void addUser() {
		String name = txtName.getText();
		String surname = txtSurname.getText();
		String email = txtEmail.getText();
		String password = txtPass.getText();

		if (name.isBlank() || surname.isBlank() || email.isBlank() || password.isBlank()) {
			lblStatus.setText("All fields must be filled!");
			return;
		}

		userService.createUser(email, name, surname, password);

		if (currSelectedUser == null) {
			lblStatus.setText("Failed to add user!");
			return;
		}

		addAccount(AccountType.ALGAS_KONTS);

		Database.log(Integer.toString(userService.getCurrentUser().getUserID()), "added new user " + email);
		lblStatus.setText("Added user!");
		clearAll();
	}

	@FXML
	public void changePassword() {
		String newPassword = txtPass.getText();
		if (newPassword == null || newPassword.isBlank()) {
			lblStatus.setText("Password cannot be empty");
			return;
		}

		String hashedPass = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());
		currSelectedUser.setHashedPassword(hashedPass);

		database.updateUser(currSelectedUser);
		lblStatus.setText("Changed password!");
		Database.log(Integer.toString(userService.getCurrentUser().getUserID()), "changed password for " + txtID.getText());
	}

	@FXML
	public void modifyUser() {
		String idString = txtID.getText();
		String name = txtName.getText();
		String surname = txtSurname.getText();
		String email = txtEmail.getText();

		if (idString.isBlank() || name.isBlank() || surname.isBlank() || email.isBlank()) {
			lblStatus.setText("All fields must be filled!");
			return;
		}

		int id;
		try {
			id = Integer.parseInt(idString);
		} catch (NumberFormatException e) {
			lblStatus.setText("Invalid ID format!");
			return;
		}

		currSelectedUser.setUserID(id);
		currSelectedUser.setName(name);
		currSelectedUser.setSurname(surname);
		currSelectedUser.setEmail(email);

		database.updateUser(currSelectedUser);
		lblStatus.setText("Modified user!");
		Database.log(Integer.toString(userService.getCurrentUser().getUserID()), "modified user " + idString);
	}

	@FXML
	public void deleteUser() {
		int id = currSelectedUser.getUserID();
		database.deleteUser(id);
		lblStatus.setText("Deleted user!");
		Database.log(Integer.toString(userService.getCurrentUser().getUserID()), "deleted user " + id);
		clearAll();
	}

	@FXML
	public void addAlgasKonts() {
		addAccount(AccountType.ALGAS_KONTS);
	}

	@FXML
	public void addKreditaKonts() {
		addAccount(AccountType.KREDITA_KONTS);
	}

	@FXML
	public void addNoguldijumaKonts() {
		addAccount(AccountType.NOGULDIJUMA_KONTS);
	}

	public void addAccount(AccountType type) {

		if (currSelectedUser == null) {
			lblStatus.setText("No user selected!");
			return;
		}

		try {
			int userId = currSelectedUser.getUserID();
			Account acc = new Account(BigDecimal.ZERO, Currency.getInstance("EUR"), 0, type, userId);
			database.addAccount(acc);
			Database.log(Integer.toString(userService.getCurrentUser().getUserID()), "added " + type.getName() + " for " + userId);
			lblStatus.setText("Added " + acc.getAccountType().getName() + "!");
			setFields();
		} catch (NumberFormatException e) {
			lblStatus.setText("Invalid User ID format!");
		}
	}

	@FXML
	public void selectionChanged() {
		int selectedIndex = tableAccounts.getSelectionModel().getSelectedIndex();

		if (selectedIndex != -1) {
			btnDeleteAccount.setDisable(false);
		} else {
			btnDeleteAccount.setDisable(true);
		}
	}

	@FXML
	public void deleteAccountPressed() {
		if (tableAccounts.getItems().size() <= 1) {
			lblStatus.setText("Vajag vismaz vienu kontu");
			return;
		}

		Account acc = tableAccounts.getSelectionModel().getSelectedItem();
		database.removeAccount(acc.getAccountNumber());
		lblStatus.setText("Deleted user account!");
		setFields();
	}

	private void disableKontsButtons(boolean status) {
		btnAddAlgasKonts.setDisable(status);
		btnAddKreditaKonts.setDisable(status);
		btnAddNoguldijumaKonts.setDisable(status);
	}

	private void clearFields() {
		tableAccounts.setItems(FXCollections.observableArrayList());
		txtName.setText("");
		txtSurname.setText("");
		txtEmail.setText("");
		txtPass.setText("");
	}

	private void setFields() {
		if (currSelectedUser == null) {
			disableKontsButtons(true);
			return;
		}

		txtName.setText(currSelectedUser.getName());
		txtSurname.setText(currSelectedUser.getSurname());
		txtEmail.setText(currSelectedUser.getEmail());
		txtPass.setText("");
		var accounts = database.getAccounts(currSelectedUser.getUserID());
		tableAccounts.setItems(FXCollections.observableArrayList(accounts));
		disableKontsButtons(false);
	}

	private void clearAll() {
		txtID.setText("");
		clearFields();
	}
}
