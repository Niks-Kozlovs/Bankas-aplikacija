package Database;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import Accounts.AccountTemplate;
import Accounts.AlgasKonts;
import Accounts.KreditaKonts;
import Accounts.NoguldijumaKonts;
import User.User;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Database {
	private java.sql.Connection myConn;

	private static final String URL = "jdbc:mysql://localhost:3306/bankapp";
	private static final String USERNAME = "root";
	private static final String PASS = "";

	public Database() {
		connect();
		createStatement();
	}

	private void connect() {
		try {
			myConn = DriverManager.getConnection(URL, USERNAME, PASS);
		} catch (SQLException exc) {
			exc.printStackTrace();
			showError();
		}
	}

	private void createStatement() {
		try {
			myConn.createStatement();
		} catch (SQLException e) {
			e.printStackTrace();
			showError();
		}
	}

	private static void showError() {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("Database failed to connect");
		alert.setContentText("Check your internet connection or make sure the server is turned on!");

		alert.showAndWait();
	}

	public boolean addNewUser(String name, String surname, String email, String password) {
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement(
					"INSERT INTO `users`(`ID`, `Name`, `Surname`, `Email`, `Password`) VALUES ('0',?,?,?,?);");
			stmt.setString(1, name);
			stmt.setString(2, surname);
			stmt.setString(3, email);
			stmt.setString(4, BCrypt.hashpw(password, BCrypt.gensalt()));
			stmt.executeUpdate();

			// For now there is only EUR
			addAccount(getUser(name, surname, email).getUserID(), AccountTemplate.ALGAS_KONTS, (float) 0.00, "EUR");
			
			return true;

		} catch (SQLException e) {
			return false;
		}
	}

	public void changePassword(int id, String password) {
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("UPDATE users SET Password = ? WHERE ID = ?");
			stmt.setString(1, BCrypt.hashpw(password, BCrypt.gensalt()));
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
			log(who, "Deleted user" + name + " " + surname + " " + email);
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}
	}

	public boolean login(String email, String password, User user) {
		ResultSet rs = null;
		boolean isAdmin = false;
		int userCount = 0;
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("SELECT * FROM `users` WHERE Email = ?");
			stmt.setString(1, email);
			rs = stmt.executeQuery();

			while (rs.next()) {

				String hashedPass = rs.getString("Password");
				if (BCrypt.checkpw(password, hashedPass)) {
					if (isAdmin(rs.getInt("ID"))) {
						isAdmin = true;
					}
					user.setAll(rs.getInt("ID"), rs.getString("email"), rs.getString("name"), rs.getString("surname"),
							isAdmin);
					userCount++;
				}
			}

		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}

		// If there are no users or are more than one (somehow) then returns false
		if (userCount == 1) {
			String buffer = "Logged in";

			// If the user is admin then it will log it later
			if (!isAdmin) {
				log(user.getUserID() + "", buffer);
			}

			return true;
		}
		return false;

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

	public void addAccount(int owner, int type, float value, String currency) {
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement(
					"INSERT INTO `accounts`(`Number`, `Owner`, `Type`, `Value`, `Currency`) VALUES ('0',?,?,?,?);");
			stmt.setInt(1, owner);
			stmt.setInt(2, type);
			stmt.setFloat(3, value);
			stmt.setString(4, currency);

			stmt.executeUpdate();
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}
	}

	public void addAccount(int owner, AccountTemplate acc) {
		addAccount(owner, acc.getAccountType(), acc.getMoney().floatValue(), acc.getMoneyType());
	}

	public ResultSet getAccounts(int owner) {
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("SELECT * from accounts WHERE Owner = ?");
			stmt.setInt(1, owner);

			return stmt.executeQuery();
		} catch (SQLException e) {
			showError();
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

	// Proud of this method
	public boolean sendMoney(int fromWho, float amount, int toWho) {
		// Account information after getting it from DB
		AccountTemplate accReceiver, accSender;
		// Ja nav naudas

		// Get account info for updating
		accReceiver = getAccountInfo(toWho);
		accSender = getAccountInfo(fromWho);

		if (accSender.getMoney().floatValue() - amount < 0) {
			return false;
		}

		if (accReceiver == null || accSender == null)
			return false;

		if (isSameOwner(fromWho, toWho)) {
			accReceiver.addMoney(amount);
			accSender.removeMoney(amount);
		} else {
			accReceiver.receiveMoney(amount);
			accSender.sendMoney(amount);
		}
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

	public boolean addMoney(int accountNumber, float amount) {
		AccountTemplate acc;
		acc = getAccountInfo(accountNumber);

		if (acc == null) {
			return false;
		}

		acc.addMoney(amount);
		updateAccountInDB(acc);
		return true;
	}

	public boolean removeMoney(int accountNumber, float amount) {
		return addMoney(accountNumber, (amount * -1));
	}

	private AccountTemplate getAccountInfo(int accountNumber) {
		ResultSet result;
		AccountTemplate acc = null;
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("SELECT * FROM `accounts` WHERE Number = ?");
			stmt.setInt(1, accountNumber);
			result = stmt.executeQuery();
			result.next();
			acc = createAccountBasedOnType(result.getInt("Type"));
			acc.setAccountType(result.getInt("Type"));
			acc.setMoney(result.getBigDecimal("Value"));
			acc.setAccountNumber(accountNumber);

			return acc;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}

	}

	private void updateAccountInDB(AccountTemplate acc) {
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("UPDATE accounts SET Value = ? WHERE Number = ?");
			stmt.setFloat(1, acc.getMoney().floatValue());
			stmt.setInt(2, acc.getAccountNumber());
			stmt.executeUpdate();

		} catch (SQLException e) {
			showError();
			e.printStackTrace();
		}

	}

	// For using addmoney functions which isnt in the abstract class
	public static AccountTemplate createAccountBasedOnType(int type) {
		switch (type) {
		case AccountTemplate.ALGAS_KONTS:
			return new AlgasKonts();
		case AccountTemplate.KREDITA_KONTS:
			return new KreditaKonts();
		case AccountTemplate.NOGULDIJUMA_KONTS:
			return new NoguldijumaKonts();
		default:
			return null;
		}
	}

	public boolean loginAdmin(int userID, String password) {
		ResultSet rs = null;
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement("SELECT * from admins WHERE UserID = ?");
			stmt.setInt(1, userID);
			rs = stmt.executeQuery();
			String hashedPass;
			if (rs.next()) {
				hashedPass = rs.getString("AdminPass");
				if (BCrypt.checkpw(password, hashedPass)) {
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
}
