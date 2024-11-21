package Controller;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import Model.User;
import Model.Accounts.Account;
import Model.Accounts.AccountType;
import Services.TransactionService;
import Services.UserService;
import Util.InputFormatter;
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
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
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
	private Label lblStatus;

	@FXML
	private TableView<Account> listAccounts;
	@FXML
	private TableColumn<Account, Integer> accountNumberColumn;
	@FXML
	private TableColumn<Account, BigDecimal> balanceColumn;
	@FXML
	private TableColumn<Account, String> currencyColumn;
	@FXML
	private TableColumn<Account, String> accountTypeColumn;

	@FXML
	private ComboBox<String> filterBox;

	@FXML
	private TextField txtTransferNr;

	@FXML
	private TextField txtAddMoney;

	@FXML
	private TextField txtTransferAmount;
	@FXML
	private TextField txtAddRemoveAmount;

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

        Platform.runLater(() -> {
            Stage stage = (Stage) btnTransfer.getScene().getWindow();
            stage.setTitle("Welcome " + user.getName() + "! (" + user.getUserID() + ")");
			stage.getIcons().add(new Image(getClass().getResourceAsStream("/res/Logo.jpg")));
			updateStats();
        });

		selectedAccountNumber.addListener((obs, oldSelection, newSelection) -> {
			if (newSelection.equals(-1)) {
				setFieldsAndButtons(true);
				return;
			}

			setFieldsAndButtons(false);
		});

		populateAccountsList();

		String[] accountNames = Stream.of(AccountType.values())
			.map(AccountType::getName)
			.toArray(String[]::new);

		filterBox.getItems().addAll(accountNames);
		filterBox.getItems().add("All");

		setFieldsAndButtons(true);

		txtTransferNr.setTextFormatter(InputFormatter.getOnlyDigitsFormatter());
		txtTransferAmount.setTextFormatter(InputFormatter.getOnlyDoubleTextFormatter());
		txtAddRemoveAmount.setTextFormatter(InputFormatter.getOnlyDoubleTextFormatter());
	}

	private void populateAccountsList() {
		accountNumberColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getAccountNumber()).asObject());
        balanceColumn.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getBalance()));
        currencyColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCurrencySymbol()));
        accountTypeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getAccountName()));

        accountNumberColumn.setPrefWidth(25);
        balanceColumn.setPrefWidth(25);
        currencyColumn.setPrefWidth(25);
        accountTypeColumn.setPrefWidth(25);

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

		updateStats();
	}

	private void updateStats() {
		BigDecimal totalMoney = filteredAccounts.stream()
			.map(Account::getBalance)
			.reduce(BigDecimal.ZERO, BigDecimal::add);

		int accountCount = filteredAccounts.size();

		lblMoney.setText("Sum of money for selected accounts is " + totalMoney);
		lblCount.setText("There are " + accountCount + " accounts");
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

		BigDecimal money = getTransferMoney();

		if (money.compareTo(BigDecimal.ZERO) <= 0) {
			lblStatus.setText("Invalid transfer amount");
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
		BigDecimal money = getAddRemoveMoney();
		if (money.compareTo(BigDecimal.ZERO) <= 0) {
			lblStatus.setText("Invalid amount");
			return;
		}
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
		btnTransfer.setDisable(isDisabled);
		btnAddMoney.setDisable(isDisabled);
		btnRemoveMoney.setDisable(isDisabled);
	}

	@FXML
	public void openAdminPanel() {
		TextInputDialog dialog = new TextInputDialog();
		dialog.setTitle("Admin password");
		dialog.setHeaderText("Enter admin password");
		dialog.setContentText("Password: ");
		Optional<String> result = dialog.showAndWait();

		if (result.isEmpty()) {
			lblStatus.setText("No password entered");
			return;
		}

		String adminPassword = result.get();
		boolean isAdmin = userService.checkIsAdmin(adminPassword);

		if (!isAdmin) {
			lblStatus.setText("Invalid admin password");
			return;
		}

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

	private BigDecimal getTransferMoney() {
		try {
			return new BigDecimal(txtTransferAmount.getText());
		} catch (NumberFormatException e) {
			lblStatus.setText("Invalid transfer amount");
			return BigDecimal.ZERO;
		}
	}

	private BigDecimal getAddRemoveMoney() {
		try {
			return new BigDecimal(txtAddRemoveAmount.getText());
		} catch (NumberFormatException e) {
			lblStatus.setText("Invalid amount");
			return BigDecimal.ZERO;
		}
	}
}
