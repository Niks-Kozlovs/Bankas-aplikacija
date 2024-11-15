package Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;
import java.util.Currency;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import Model.User;
import Model.Accounts.Account;
import Model.Accounts.AccountType;
import Services.Database;
import Services.UserService;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class MainFormController implements Initializable {
	Database db = Database.getInstance();
	UserService userService = UserService.getInstance();
	User user;
	SimpleIntegerProperty selectedAccountNumber = new SimpleIntegerProperty(-1);
	ObservableList<Account> accounts = FXCollections.observableArrayList(userService.getUserAccounts());
	FilteredList<Account> filteredAccounts = new FilteredList<>(accounts);

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
	private TableView<Account> listAccounts = new TableView<Account>();

	@FXML
	private ComboBox<String> filterBox;

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
	@FXML
	private Button btnOpenAdmin;

	private static final String NO_TRANSFER = "Could not transfer money!";

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		this.user = userService.getCurrentUser();

		if (!this.user.getIsAdmin()) {
			btnOpenAdmin.setVisible(false);
		}


		selectedAccountNumber.addListener((obs, oldSelection, newSelection) -> {
			if (newSelection.equals(-1)) {
				setFieldsAndButtons(true);
				lblAccount.setText("Selected account: none");
				return;
			}

			int accountNumber = newSelection.intValue();
			// Account selectedAccount = accounts.stream()
			// 	.filter(account -> account.getAccountNumber() == accountNumber)
			// 	.findFirst()
			// 	.orElse(null);
			setFieldsAndButtons(false);
			lblAccount.setText("Selected account: " + accountNumber);
		});

		populateAccountsList();

		lblAccount.setText("Selected account: none");
		lblName.setText("Welcome " + user.getName() + "! (" + user.getUserID() + ")");
		String[] accountNames = Stream.of(AccountType.values())
			.map(AccountType::getName)
			.toArray(String[]::new);

		filterBox.getItems().addAll(accountNames);
		filterBox.getItems().add("All");

		setFieldsAndButtons(true);
	}

	private void populateAccountsList() {
		TableColumn<Account, Integer> accountNumber = new TableColumn<Account, Integer>("Account number");
		accountNumber.setCellValueFactory(new PropertyValueFactory<Account, Integer>("accountNumber"));

		TableColumn<Account, BigDecimal> money = new TableColumn<Account, BigDecimal>("Balance");
		money.setCellValueFactory(new PropertyValueFactory<Account, BigDecimal>("balance"));

		TableColumn<Account, Currency> moneyType = new TableColumn<Account, Currency>("Currency");
		moneyType.setCellValueFactory(new PropertyValueFactory<Account, Currency>("moneyType"));

		TableColumn<Account, String> accountType = new TableColumn<Account, String>("Account type");
		accountType.setCellValueFactory(new PropertyValueFactory<Account, String>("accountName"));

		List<TableColumn<Account, ?>> columns = Arrays.asList(accountNumber, money, moneyType, accountType);
		listAccounts.getColumns().addAll(columns);

		listAccounts.setItems(filteredAccounts);
	}

	@FXML
	public void onAccountTypeChanged() {
		String selectedType = filterBox.getSelectionModel().getSelectedItem();
		filteredAccounts.setPredicate(account -> {
			if (selectedType.equals("All")) {
				return true;
			}
			return account.getAccountName().equals(selectedType);
		});
	}

	@FXML
	public void selectionChanged() {
		Account selectedAccount = listAccounts.getSelectionModel().getSelectedItem();
		if (selectedAccount == null) {
			selectedAccountNumber.set(-1);
			return;
		}

		selectedAccountNumber.set(selectedAccount.getAccountNumber());
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
		// String account = txtTransferNr.getText();
		// String moneyStr = txtAddMoney.getText();
		
		// if (!(InputValidator.checkInteger(account) && InputValidator.checkFloat(moneyStr))) {
		// 	lblStatus.setText("Invalid account number or money!");
		// 	return;
		// }
		// int selectedAccountNumber = selectedAccount.getAccountNumber();
		// int accountToTransferTo = Integer.parseInt(account);
		// float money = Float.parseFloat(moneyStr);
		
		// if (money <= 0) {
		// 	lblStatus.setText("Money is negative!");
		// 	return;
		// }

		// if (db.sendMoney(selectedAccountNumber, money, accountToTransferTo)) {
		// 	refreshTableafterTransfer(selectedAccountNumber, accountToTransferTo, money);
		// 	listAccounts.refresh();
		// 	Database.log(Integer.toString(user.getUserID()),
		// 			"transfered money from " + selectedAccountNumber + " to" + accountToTransferTo + "(" + money + ")");

		// } else {
		// 	lblStatus.setText(NO_TRANSFER);
		// }

		// updateStats();
	}

	@FXML
	public void addMoneyClicked() {
		// String moneyStr = txtAddMoney.getText();
		// if (!InputValidator.checkFloat(moneyStr)) {
		// 	lblStatus.setText("Invalid money entered!");
		// 	return;
		// }
		// float money = Float.parseFloat(moneyStr);
		// int account = selectedAccount.getAccountNumber();
		
		// if (money <= 0) {
		// 	lblStatus.setText("Money is negative!");
		// 	return;
		// }

		// if (db.addMoney(account, money)) {
		// 	refreshTableafterTransfer(-1, account, money);
		// 	listAccounts.refresh();
		// 	Database.log(Integer.toString(user.getUserID()), "added money to " + account + ": " + money);
		// } else {
		// 	lblStatus.setText(NO_TRANSFER);
		// }

		// updateStats();

	}

	@FXML
	public void removeMoneyClicked() {
		// String moneyStr = txtAddMoney.getText();
		// if (!InputValidator.checkFloat(moneyStr)) {
		// 	lblStatus.setText("Invalid money entered!");
		// 	return;
		// }
		// float money = Float.parseFloat(moneyStr);
		// if (money <= 0) {
		// 	lblStatus.setText("Money is negative!");
		// 	return;
		// }
		
		// int account = selectedAccount.getAccountNumber();
		// if (db.removeMoney(account, money)) {
		// 	refreshTableafterTransfer(account, -1, money);
		// 	listAccounts.refresh();
		// 	Database.log(Integer.toString(user.getUserID()), "removed money from " + account + ": " + money);
		// } else {
		// 	lblStatus.setText(NO_TRANSFER);
		// }

		// updateStats();
	}

	@FXML
	public void openAdminPanel() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/View/AdminPage.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) btnOpenAdmin.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
	}

	public void shutdown() {
		Platform.exit();
	}
}
