package Controller;

import java.math.BigDecimal;
import java.net.URL;
import java.util.Currency;
import java.util.ResourceBundle;

import Model.Database;
import Model.User;
import Model.Accounts.AccountTemplate;
import Model.Accounts.Konti;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class MainFormController implements Initializable {
	User user;
	Database db;
	AccountTemplate selectedAccount = null;

	@FXML
	private Label lblName;
	@FXML
	private Label lblMoney;
	@FXML
	private Label lblCount;
	@FXML
	private Label lblAccount;
	@FXML
	private Label lblStatus;

	@FXML
	private TableView<AccountTemplate> listAccounts = new TableView<>();

	@FXML
	private ComboBox<String> filterBox; // Its a combobox for filtering the accounts in the list

	@FXML
	private TextField txtTransferNr;

	@FXML
	private TextField txtAddMoney;

	@FXML
	private Button btnTransfer;

	@FXML
	private Button btnAddMoney;
	@FXML
	private Button btnRemoveMoney;
	
	private static final String NO_TRANSFER = "Could not transfer money!";

	// Constructor
	public void setUp(User user, Database db) {
		this.db = db;
		this.user = user;
		lblAccount.setText("Selected account: none");

		lblName.setText("Welcome " + user.getName() + "! (" + user.getUserID() + ")");
		populateAccountsList(user.getAccounts());
		// Populate combobox
		filterBox.getItems().addAll("Algas konts", "Noguldijuma konts", "Kredita konts", "All");
		// Gets the first account
		selectedAccount = getAccounts(user.getAccounts()).get(0);
		listAccounts.getSelectionModel().selectFirst();
		
	}

	@SuppressWarnings("unchecked")
	private void populateAccountsList(Konti accounts) {
		TableColumn<AccountTemplate, Integer> accountNumber = new TableColumn<>("Account number");
		accountNumber.setMinWidth(100);
		accountNumber.setCellValueFactory(new PropertyValueFactory<>("accountNumber"));

		TableColumn<AccountTemplate, BigDecimal> money = new TableColumn<>("Money");
		money.setCellValueFactory(new PropertyValueFactory<>("money"));

		TableColumn<AccountTemplate, Currency> moneyType = new TableColumn<>("Currency");
		moneyType.setCellValueFactory(new PropertyValueFactory<>("moneyType"));

		TableColumn<AccountTemplate, String> accountType = new TableColumn<>("Account type");
		accountType.setCellValueFactory(new PropertyValueFactory<>("accountTypeString"));

		listAccounts.setItems(getAccounts(accounts));
		listAccounts.getColumns().addAll(accountNumber, money, moneyType, accountType);
		updateStats();
	}

	// Sets up the stats for the filtered accounts
	// if noguldijuma konts is selected then only stats for those accounts will be
	// shown
	// to see all the stats there is an all filter option
	private void updateStats() {
		BigDecimal money = new BigDecimal(0);
		int count = 0;
		String moneyType = "EUR";

		for (AccountTemplate item : listAccounts.getItems()) {
			count++;
			money = money.add(item.getMoney());
		}
		if (count > 0)
			moneyType = listAccounts.getItems().get(0).getMoneyType();

		// Rounds to two decimal points like all money is in modern society
		money.setScale(2);

		String filterBoxValue = filterBox.getValue();
		// When the window is first the comboboxes value is null
		if (filterBoxValue == null) {
			filterBoxValue = "all";
		}

		lblMoney.setText(
				"Sum of money for " + filterBoxValue.toLowerCase() + " is " + money.toString() + " " + moneyType);
		lblCount.setText("There are " + count + " accounts");
	}
	
	//Gets all the arraylists in the user object and turns it into one observable list for the table
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

	// If it is for adding/removing money then set the account number you donw want to use to -1
	public void refreshTableafterTransfer(int accNrTransferedFrom, int accNrTransferedTo, float money) {

		ObservableList<AccountTemplate> acc = getAccounts(user.getAccounts());

		for (int i = 0; i < acc.size(); i++) {
			if (accNrTransferedTo != -1 && acc.get(i).getAccountNumber() == accNrTransferedTo) {
				AccountTemplate accTemp = acc.get(i);
				accTemp.addMoney(money);
				acc.set(i, accTemp);
			}

			if (accNrTransferedFrom != -1 && acc.get(i).getAccountNumber() == accNrTransferedFrom) {
				AccountTemplate accTemp = acc.get(i);
				accTemp.removeMoney(money);
				acc.set(i, accTemp);
			}
		}

		user.setAccounts(acc);
		listAccounts.setItems(acc);
	}
	
	//Combobox changed selection
	@FXML
	public void updateList() {
		ObservableList<AccountTemplate> accounts = getAccounts(user.getAccounts());
		lblAccount.setText("Selected account: none");
		if (listAccounts.getItems().size() > 0) {
			setFieldsAndButtons(true);
		}
		if (filterBox.getValue().equals("All")) {
			listAccounts.setItems(accounts);
			updateStats();
			return;
		}

		ObservableList<AccountTemplate> filteredAccounts = FXCollections.observableArrayList();
		;
		for (int i = 0; i < accounts.size(); i++) {
			// If account type equals selected
			if (accounts.get(i).getAccountTypeString().equals(filterBox.getValue())) {
				filteredAccounts.add(accounts.get(i));
			}
		}

		listAccounts.setItems(filteredAccounts);
		updateStats();
	}

	
	//Table changed the selection (clicked on account)
	@FXML
	public void selectionChanged() {
		int selectedIndex = listAccounts.getSelectionModel().getSelectedIndex();

		if (selectedIndex != -1) {
			setFieldsAndButtons(false);
			selectedAccount = listAccounts.getItems().get(selectedIndex);
			lblAccount.setText("Selected account: " + selectedAccount.getAccountNumber());
		} else {
			setFieldsAndButtons(true);
			lblAccount.setText("Selected account: none");
		}
	}
	
	private void setFieldsAndButtons(boolean bool) {
		txtTransferNr.setDisable(bool);
		txtAddMoney.setDisable(bool);
		btnTransfer.setDisable(bool);
		btnAddMoney.setDisable(bool);
		btnRemoveMoney.setDisable(bool);
	}

	@FXML
	public void transferClicked() {
		String account = txtTransferNr.getText();
		String moneyStr = txtAddMoney.getText();
		
		if (!(InputControl.checkInteger(account) && InputControl.checkFloat(moneyStr))) {
			lblStatus.setText("Invalid account number or money!");
			return;
		}
		int selectedAccountNumber = selectedAccount.getAccountNumber();
		int accountToTransferTo = Integer.parseInt(account);
		float money = Float.parseFloat(moneyStr);
		
		if (money <= 0) {
			lblStatus.setText("Money is negative!");
			return;
		}

		if (db.sendMoney(selectedAccountNumber, money, accountToTransferTo)) {
			refreshTableafterTransfer(selectedAccountNumber, accountToTransferTo, money);
			listAccounts.refresh();
			Database.log(Integer.toString(user.getUserID()),
					"transfered money from " + selectedAccountNumber + " to" + accountToTransferTo + "(" + money + ")");

		} else {
			lblStatus.setText(NO_TRANSFER);
		}

		updateStats();
	}

	@FXML
	public void addMoneyClicked() {
		String moneyStr = txtAddMoney.getText();
		if (!InputControl.checkFloat(moneyStr)) {
			lblStatus.setText("Invalid money entered!");
			return;
		}
		float money = Float.parseFloat(moneyStr);
		int account = selectedAccount.getAccountNumber();
		
		if (money <= 0) {
			lblStatus.setText("Money is negative!");
			return;
		}

		if (db.addMoney(account, money)) {
			refreshTableafterTransfer(-1, account, money);
			listAccounts.refresh();
			Database.log(Integer.toString(user.getUserID()), "added money to " + account + ": " + money);
		} else {
			lblStatus.setText(NO_TRANSFER);
		}

		updateStats();

	}

	@FXML
	public void removeMoneyClicked() {
		String moneyStr = txtAddMoney.getText();
		if (!InputControl.checkFloat(moneyStr)) {
			lblStatus.setText("Invalid money entered!");
			return;
		}
		float money = Float.parseFloat(moneyStr);
		if (money <= 0) {
			lblStatus.setText("Money is negative!");
			return;
		}
		
		int account = selectedAccount.getAccountNumber();
		if (db.removeMoney(account, money)) {
			refreshTableafterTransfer(account, -1, money);
			listAccounts.refresh();
			Database.log(Integer.toString(user.getUserID()), "removed money from " + account + ": " + money);
		} else {
			lblStatus.setText(NO_TRANSFER);
		}

		updateStats();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
	}
	
	//Closes all open windows for program and shuts it down
	public void shutdown() {
		Platform.exit();
	}
}
