package Controller;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Currency;
import java.util.ResourceBundle;

import Model.Database;
import Model.User;
import Model.Accounts.AccountTemplate;
import Model.Accounts.Konti;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class AdminPageController implements Initializable {
	User user;
	Database db;
	// Selected user
	User userContainer = new User();

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
	private TableView<AccountTemplate> tableAccounts = new TableView<>();;

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
	private Label lblStatus = new Label();

	private AccountTemplate acc = null;

	// The constructor
	@SuppressWarnings("unchecked")
	public void setUp(User user, Database db) {
		this.db = db;
		this.user = user;
		btnDeleteAccount.setDisable(true);
		disableKontsButtons(true);

		TableColumn<AccountTemplate, Integer> accountNumber = new TableColumn<>("Account number");
		accountNumber.setMinWidth(100);
		accountNumber.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

		TableColumn<AccountTemplate, BigDecimal> money = new TableColumn<>("Money");
		money.setCellValueFactory(new PropertyValueFactory<>("money"));

		TableColumn<AccountTemplate, Currency> moneyType = new TableColumn<>("Currency");
		moneyType.setCellValueFactory(new PropertyValueFactory<>("moneyType"));

		TableColumn<AccountTemplate, String> accountType = new TableColumn<>("Account type");
		accountType.setCellValueFactory(new PropertyValueFactory<>("accountTypeString"));

		tableAccounts.getColumns().addAll(accountNumber, money, moneyType, accountType);

	}

	private void populateAccountsList(Konti accounts) {
		tableAccounts.setItems(null);
		tableAccounts.setItems(getAccounts(accounts));

	}

	private ObservableList<AccountTemplate> getAccounts(Konti accounts) {
		ObservableList<AccountTemplate> acc = FXCollections.observableArrayList();
		for (int i = 0; i < accounts.getAlgasKonti().size(); i++) {
			acc.add(accounts.getAlgasKonti().get(i));
		}

		for (int i = 0; i < accounts.getNoguldijumaKonti().size(); i++) {
			acc.add(accounts.getNoguldijumaKonti().get(i));

		}

		for (int i = 0; i < accounts.getKreditaKonti().size(); i++) {
			acc.add(accounts.getKreditaKonti().get(i));

		}
		return acc;
	}

	// When texts get added to the texftied id it updates it instantly
	// The only negative thing it would spam request to the database
	// but since there isnt a lot of admins it wont take that much
	public void setTextFields() {
		// if it is invalid then clear the fields
		if (!InputControl.checkInteger(txtID.getText()) || txtID.getText().equals("")) {
			clearFields();
			disableKontsButtons(true);
			return;
		}

		userContainer = db.getUser(Integer.parseInt(txtID.getText()));
		if (userContainer == null) {
			clearFields();
			disableKontsButtons(true);
			return;
		} else {
			disableKontsButtons(false);
		}

		txtName.setText(userContainer.getName());
		txtSurname.setText(userContainer.getSurname());
		txtEmail.setText(userContainer.getEmail());
		Konti konti = new Konti();
		konti.addAccounts(db.getAccounts(userContainer.getUserID()));
		populateAccountsList(konti);
	}
	
	private void disableKontsButtons(boolean status) {

		btnAddAlgasKonts.setDisable(status);
		btnAddKreditaKonts.setDisable(status);
		btnAddNoguldijumaKonts.setDisable(status);
	}

	public void addUser() {
		String name = txtName.getText();
		String surname = txtSurname.getText();
		String email = txtEmail.getText();
		String password = txtPass.getText();

		if (!InputControl.isValidNameSurname(name) || !InputControl.isValidNameSurname(surname) || !InputControl.checkEmail(email)) {
			lblStatus.setText("Please check if all the fields are entered correctly!");
			return;
		} else {
			name = InputControl.capitalizeFirstLetter(name);
			surname = InputControl.capitalizeFirstLetter(surname);

			if (db.addNewUser(name, surname, email, password) != null) {
				Database.log(Integer.toString(user.getUserID()), "added account");
				lblStatus.setText("Added user!");
				clearAll();
			} else {
				//Does not clear fields for the admin  to change the wrong answer
				lblStatus.setText("Email already exists");
			}
			
			
		}
	}

	// Password can have anything in it so no checks
	public void changePassword() {
		db.changePassword(Integer.parseInt(txtID.getText()), txtPass.getText());
		lblStatus.setText("Changed password!");
		Database.log(Integer.toString(user.getUserID()), "changed password for " + txtID.getText());
	}

	public void modifyUser() {
		String ID = txtID.getText();
		String name = txtName.getText();
		String surname = txtSurname.getText();
		String email = txtEmail.getText();
		if (!(InputControl.checkInteger(ID) || InputControl.isValidNameSurname(name) || InputControl.isValidNameSurname(surname) || InputControl.checkEmail(email))) {
			lblStatus.setText("Invalid values entered please check fields!");
			return;
		}
		name = InputControl.capitalizeFirstLetter(name);
		surname = InputControl.capitalizeFirstLetter(surname);
		
		db.modifyUser(Integer.parseInt(ID), name, surname, email);
		lblStatus.setText("Modified user!");
		Database.log(Integer.toString(user.getUserID()), "modified user " + ID);
	}

	public void deleteUser() {
		String id = txtID.getText();
		if (!InputControl.checkInteger(id)) {
			lblStatus.setText("Check if id is correct!");
			return;
		}
		db.deleteUser(Integer.parseInt(id));
		lblStatus.setText("Deleted user!");
		Database.log(Integer.toString(user.getUserID()), "deleted user " + id);
		clearAll();
	}

	public void addAlgasKonts() {
		db.addAccount(Integer.parseInt(txtID.getText()), AccountTemplate.ALGAS_KONTS, (float) 0.00, "EUR");
		updateAccounts();
		lblStatus.setText("Added algas konts!");
		Database.log(Integer.toString(user.getUserID()), "added algas konts for " + txtID.getText());
	}

	public void addKreditaKonts() {
		db.addAccount(Integer.parseInt(txtID.getText()), AccountTemplate.KREDITA_KONTS, (float) 0.00, "EUR");
		updateAccounts();
		lblStatus.setText("Added kredita konts!");
		Database.log(Integer.toString(user.getUserID()), "added kredita konts for " + txtID.getText());
	}

	public void addNoguldijumaKonts() {
		db.addAccount(Integer.parseInt(txtID.getText()), AccountTemplate.NOGULDIJUMA_KONTS, (float) 0.00, "EUR");
		updateAccounts();
		lblStatus.setText("Added noguldijuma konts!");
		Database.log(Integer.toString(user.getUserID()), "added noguldijuma konts for " + txtID.getText());
	}

	private void updateAccounts() {
		Konti konti = new Konti();
		konti.addAccounts(db.getAccounts(Integer.parseInt(txtID.getText())));
		populateAccountsList(konti);
	}

	// Table account selection changed
	@FXML
	public void selectionChanged() {
		int selectedIndex = tableAccounts.getSelectionModel().getSelectedIndex();
		
		if (selectedIndex != -1) {
			btnDeleteAccount.setDisable(false);
			acc = tableAccounts.getItems().get(selectedIndex);
		} else {
			btnDeleteAccount.setDisable(true);
		}

	}

	public void deleteAccountPressed() {
		if (tableAccounts.getItems().size() <= 1) {
			lblStatus.setText("Vajag vismaz vienu kontu");
			return;
		}
		
		
		db.removeAccount(acc.getAccountNumber());
		updateAccounts();
		lblStatus.setText("Deleted user account!");
		Database.log(Integer.toString(user.getUserID()), "removed account " + acc.getAccountNumber());
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}

	private void clearFields() {
		tableAccounts.setItems(null);
		txtName.setText("");
		txtSurname.setText("");
		txtEmail.setText("");
		txtPass.setText("");
	}

	private void clearAll() {
		clearFields();
		txtID.setText("");

	}

}
