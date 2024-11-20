package Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import Model.User;
import Model.Accounts.Account;
import Model.Accounts.AccountType;
import Services.TransactionService;
import Services.UserService;
import Util.InputValidator;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.stage.Stage;

public class MainFormController implements Initializable {
	UserService userService = UserService.getInstance();
	TransactionService transactionService = TransactionService.getInstance();
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
	private TableView<Account> listAccounts;

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
		User user = userService.getCurrentUser();

		if (!user.getIsAdmin()) {
			btnOpenAdmin.setVisible(false);
		}


		selectedAccountNumber.addListener((obs, oldSelection, newSelection) -> {
			if (newSelection.equals(-1)) {
				setFieldsAndButtons(true);
				lblAccount.setText("Selected account: none");
				return;
			}

			int accountNumber = newSelection.intValue();
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

		txtTransferNr.setTextFormatter(InputValidator.getOnlyDigitsFormatter());
		txtAddMoney.setTextFormatter(InputValidator.getOnlyDoubleTextFormatter());
	}

	private void populateAccountsList() {
		TableColumn<Account, Integer> accountNumber = new TableColumn<Account, Integer>("Account number");
		accountNumber.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAccountNumber()).asObject());

		TableColumn<Account, BigDecimal> money = new TableColumn<Account, BigDecimal>("Balance");
		money.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBalance()));

		TableColumn<Account, String> moneyType = new TableColumn<Account, String>("Currency");
		moneyType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCurrencySymbol()));

		TableColumn<Account, String> accountType = new TableColumn<Account, String>("Account type");
		accountType.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAccountName()));

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

	@FXML
	public void transferClicked() {
		int accountNr;
		try {
			accountNr = Integer.parseInt(txtTransferNr.getText());
		} catch (NumberFormatException e) {
			lblStatus.setText("Invalid account number");
			return;
		}

		BigDecimal money = getMoney();

		if (money.equals(BigDecimal.ZERO)) {
			return;
		}

		Account selectedAccount = listAccounts.getSelectionModel().getSelectedItem();

		try {
			Account receivingAccount = transactionService.sendMoney(selectedAccount, accountNr, money);

			if (receivingAccount != null) {
				for (int i = 0; i < accounts.size(); i++) {
					if (accounts.get(i).getAccountNumber() == receivingAccount.getAccountNumber()) {
						accounts.set(i, receivingAccount);
						break;
					}
				}
			}
		} catch (IllegalArgumentException e) {
			lblStatus.setText(e.getMessage());
			return;
		} catch (Exception e) {
			lblStatus.setText(NO_TRANSFER);
			return;
		}

		accounts.set(accounts.indexOf(selectedAccount), selectedAccount);
		lblStatus.setText("Transferred " + money + " to account " + accountNr);
	}

	@FXML
	public void addMoneyClicked() {
		handleMoneyTransaction(true);
	}

	@FXML
	public void removeMoneyClicked() {
		handleMoneyTransaction(false);
	}

	private void handleMoneyTransaction(boolean isAdding) {
		BigDecimal money = getMoney();
		if (!isAdding) {
			money = money.negate();
		}
		Account selectedAccount = listAccounts.getSelectionModel().getSelectedItem();
		transactionService.addMoney(selectedAccount, money);
		accounts.set(accounts.indexOf(selectedAccount), selectedAccount);

		String action = isAdding ? "Added" : "Removed";
		String direction = isAdding ? "to" : "from";
		lblStatus.setText(action + " " + money + " " + direction + " account " + selectedAccount.getAccountNumber());
	}

	private void setFieldsAndButtons(boolean isDisabled) {
		txtTransferNr.setDisable(isDisabled);
		txtAddMoney.setDisable(isDisabled);
		btnTransfer.setDisable(isDisabled);
		btnAddMoney.setDisable(isDisabled);
		btnRemoveMoney.setDisable(isDisabled);
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

	private BigDecimal getMoney() {
		try {
			return new BigDecimal(txtAddMoney.getText());
		} catch (NumberFormatException e) {
			lblStatus.setText("Invalid money amount");
			return BigDecimal.ZERO;
		}
	}
}
