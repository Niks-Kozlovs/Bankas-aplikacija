package Services;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Currency;

import javax.naming.AuthenticationException;

import Model.User;
import Model.Accounts.AccountType;
import Model.Accounts.Account;
import at.favre.lib.crypto.bcrypt.BCrypt;

public class Database {
	private static Database instance;
	private Connection myConn;

	// TODO: Move to env file
	private static final String URL = "jdbc:mysql://localhost:3306/bankapp";
	private static final String USERNAME = "test";
	private static final String PASS = "1234";

	private Database() {
		connect();
		createUserOnFirstLaunch();
	}

	public static Database getInstance() {
		if (instance == null) {
			synchronized (Database.class) {
				if (instance == null) {
					instance = new Database();
				}
			}
		}
		return instance;
	}

	private void connect() {
		try {
			myConn = DriverManager.getConnection(URL, USERNAME, PASS);
			myConn.createStatement();
		} catch (SQLException exc) {
			exc.printStackTrace();
			showError();
		}
	}

	private void createUserOnFirstLaunch() {
		//Check the user count. If it is 0 then add a default user
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("SELECT COUNT(*) as count FROM users");
			ResultSet rs = stmt.executeQuery();
			rs.next();
			if (rs.getInt("count") != 0) {
				return;
			}

			User user = addNewUser("Admin", "Admin", "admin@admin.com", "admin");
			java.sql.PreparedStatement stmt2 = myConn.prepareStatement("INSERT INTO `admins`(`UserID`, `AdminPass`) VALUES (?,?);");
			stmt2.setInt(1, user.getUserID());
			stmt2.setString(2, BCrypt.withDefaults().hashToString(12, "admin".toCharArray()));
			stmt2.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			showError();
		}
	}

	public User addNewUser(String name, String surname, String email, String password) {
		try {
			String hashedPass = BCrypt.withDefaults().hashToString(12, password.toCharArray());
			java.sql.PreparedStatement stmt = myConn.prepareStatement(
					"INSERT INTO `users`(`ID`, `Name`, `Surname`, `Email`, `Password`) VALUES ('0',?,?,?,?);");
			stmt.setString(1, name);
			stmt.setString(2, surname);
			stmt.setString(3, email);
			stmt.setString(4, hashedPass);
			stmt.executeUpdate();

			User user = getUser(name, surname, email);
			//TODO: MOve to account service
			// addAccount(user.getUserID(), AccountType.ALGAS_KONTS, (float) 0.00, "EUR");

			return user;

		} catch (SQLException e) {
			return null;
		}
	}

	public void changePassword(int id, String password) {
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("UPDATE users SET Password = ? WHERE ID = ?");
			stmt.setString(1, BCrypt.withDefaults().hashToString(12, password.toCharArray()));
			stmt.setInt(2, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			showError();
		}
	}

	public void modifyUser(int id, String name, String surname, String email) {
		try {
			java.sql.PreparedStatement stmt = myConn
					.prepareStatement("UPDATE users SET Name = ?, Surname = ?, Email = ? WHERE ID = ?");
			stmt.setString(1, name);
			stmt.setString(2, surname);
			stmt.setString(3, email);
			stmt.setInt(4, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			showError();
		}
	}

	public User getUser(int id) {
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("Select * from users WHERE ID = ?");
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			if (!rs.next()) {
				return null;
			}

			User user = new User(rs.getInt("ID"), rs.getString("email"), rs.getString("name"), rs.getString("surname"));
			return user;
		} catch (SQLException e) {
			e.printStackTrace();
			showError();
			return null;
		}
	}

	public User getUser(String name, String surname, String email) {
		try {
			java.sql.PreparedStatement stmt = myConn
					.prepareStatement("Select * from users WHERE name  = ? AND surname = ? AND email = ?");
			stmt.setString(1, name);
			stmt.setString(2, surname);
			stmt.setString(3, email);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				User user = new User(rs.getInt("ID"), rs.getString("email"), rs.getString("name"),
						rs.getString("surname"));
				return user;
			} else {
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showError();
			return null;
		}
	}

	public void deleteUser(int id) {
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("DELETE FROM users WHERE ID = ?");
			stmt.setInt(1, id);
			stmt.executeUpdate();
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}
	}

	public void deleteUser(String who, String name, String surname, String email) {
		try {
			java.sql.PreparedStatement stmt = myConn
					.prepareStatement("DELETE FROM users WHERE Name = ? AND Surname = ? AND Email = ?");
			stmt.setString(1, name);
			stmt.setString(2, surname);
			stmt.setString(3, email);
			stmt.executeUpdate();
			//TODO: Move to admin service
			log(who, "Deleted user" + name + " " + surname + " " + email);
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}
	}

	public User login(String email, String password) throws AuthenticationException {
		ResultSet rs = null;
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("SELECT * FROM `users` WHERE Email = ?");
			stmt.setString(1, email);
			rs = stmt.executeQuery();

			if (!rs.next()) {
				throw new AuthenticationException("User not found");
			}
			//TODO: Move to user service
			String hashedPass = rs.getString("Password");
			boolean isPasswordCorrect = BCrypt.verifyer().verify(password.toCharArray(), hashedPass).verified;

			if (!isPasswordCorrect) {
				throw new AuthenticationException("Wrong password");
			}

			boolean isAdmin = isAdmin(rs.getInt("ID"));
			User user = new User(rs.getInt("ID"), rs.getString("Email"), rs.getString("Name"), rs.getString("Surname"), isAdmin);

			if (!user.getIsAdmin()) {
				log(user.getUserID() + "", "Logged in");
			} else {
				log(user.getUserID() + "", "Logged in as admin");
			}

			return user;
			} catch (SQLException e) {
				showError();
				e.printStackTrace();
			}
			return null;
		}


		private boolean isAdmin(int ID) {
		ResultSet rs;
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("SELECT * FROM admins WHERE UserID = ?");
			stmt.setInt(1, ID);
			rs = stmt.executeQuery();
			// Ja ir tabula ar tadu id
			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}
		return false;
	}

	public void addAccount(int owner, Account acc) {
				try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement(
					"INSERT INTO `accounts`(`Number`, `Owner`, `Type`, `Value`, `Currency`) VALUES ('0',?,?,?,?);");
			stmt.setInt(1, owner);
			stmt.setString(2, acc.getAccountType().name());
			stmt.setDouble(3, acc.getBalance().doubleValue());
			stmt.setString(4, acc.getMoneyType());

			stmt.executeUpdate();
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}
	}

	public ArrayList<Account> getAccounts(int owner) {
        ArrayList<Account> accounts = new ArrayList<Account>();
        String sql = "SELECT * FROM accounts WHERE Owner = ?";
        try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
            pstmt.setInt(1, owner);
			try (ResultSet rs = pstmt.executeQuery();) {
				while (rs.next()) {
					int accountNumber = rs.getInt("Number");
					BigDecimal balance = rs.getBigDecimal("Value");
					Currency currency = Currency.getInstance(rs.getString("Currency"));
					AccountType accountType = AccountType.valueOf(rs.getString("Type"));
					Account account = new Account(balance, currency, accountNumber, accountType);
					accounts.add(account);
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return accounts;
	}

	private Account getAccount(int accountNumber) {
		String sql = "SELECT * FROM `accounts` WHERE Number = ?";
		try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
			pstmt.setInt(1, accountNumber);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (!rs.next()) {
					return null;
				}

				int number = rs.getInt("Number");
				BigDecimal balance = rs.getBigDecimal("Value");
				Currency currency = Currency.getInstance(rs.getString("Currency"));
				AccountType accountType = AccountType.valueOf(rs.getString("Type"));
				return new Account(balance, currency, number, accountType);
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	public void removeAccount(int number) {
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("Delete from accounts where Number = ?");
			stmt.setInt(1, number);
			stmt.executeUpdate();
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}
	}

	//TODO: Move to account service
	// Proud of this method
	public boolean sendMoney(int fromWho, float amount, int toWho) {
		// Account information after getting it from DB
		Account accReceiver, accSender;
		// Ja nav naudas

		// Get account info for updating
		accReceiver = getAccount(toWho);
		accSender = getAccount(fromWho);

		// if (accSender.getMoney().floatValue() - amount < 0) {
		// 	return false;
		// }

		// if (accReceiver == null || accSender == null)
		// 	return false;

		// if (isSameOwner(fromWho, toWho)) {
		// 	accReceiver.addMoney(amount);
		// 	accSender.removeMoney(amount);
		// } else {
		// 	accReceiver.receiveMoney(amount);
		// 	accSender.sendMoney(amount);
		// }
		updateAccountInDB(accReceiver);
		updateAccountInDB(accSender);

		return true;

	}

	// No bonuses when tranfering between the same owner (it can be exploited)
	private boolean isSameOwner(int number1, int number2) {
		try {
			// Compares two accounts if they are the same then there will be one row in the
			// result set
			java.sql.PreparedStatement stmt = myConn.prepareStatement(
					"Select * from accounts as acc,(SELECT Owner from accounts WHERE Number = ?) as acc2 where acc.Owner = acc2.Owner AnD acc.Number = ?");
			stmt.setInt(1, number1);
			stmt.setInt(2, number2);

			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}
		return false;
	}

	//TODO: Move to account service
	public boolean addMoney(int accountNumber, float amount) {
		Account acc;
		acc = getAccount(accountNumber);

		if (acc == null) {
			return false;
		}

		// acc.addMoney(amount);
		updateAccountInDB(acc);
		return true;
	}

	//TODO: Move to account service
	public boolean removeMoney(int accountNumber, float amount) {
		return addMoney(accountNumber, (amount * -1));
	}



	private void updateAccountInDB(Account acc) {
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("UPDATE accounts SET Value = ? WHERE Number = ?");
			// stmt.setFloat(1, acc.getMoney().floatValue());
			stmt.setInt(2, acc.getAccountNumber());
			stmt.executeUpdate();

		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}

	}

	//TODO: Move to user service
	public boolean loginAdmin(int userID, String password) {
		ResultSet rs = null;
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("SELECT * from admins WHERE UserID = ?");
			stmt.setInt(1, userID);
			rs = stmt.executeQuery();
			String hashedPass;
			if (rs.next()) {
				hashedPass = rs.getString("AdminPass");
				boolean isPasswordCorrect = BCrypt.verifyer().verify(password.toCharArray(), hashedPass).verified;
				if (isPasswordCorrect) {
					log(Integer.toString(userID), "logged in as admin");
					return true;
				} else {
					return false;
				}
			}
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}

		return false;

	}

	public static void log(String name, String logMessage) {

		try {
			java.sql.Connection myConnLog = DriverManager.getConnection(URL, USERNAME, PASS);
			myConnLog.createStatement();
			java.sql.PreparedStatement stmt = myConnLog
					.prepareStatement("INSERT INTO `log` (`User`, `LogMessage`) VALUES ( ?, ?)");
			stmt.setString(1, name);
			stmt.setString(2, logMessage);
			stmt.executeUpdate();
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}
	}

	private static void showError() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("Database failed to connect");
		alert.setContentText("Check your internet connection or make sure the server is turned on!");

		alert.showAndWait();
	}
}
