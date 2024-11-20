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

import Model.User;
import Model.Accounts.AccountType;
import io.github.cdimascio.dotenv.Dotenv;
import Model.Accounts.Account;

public class Database {
	private static Database instance;
	private Connection myConn;

    private static final Dotenv dotenv = Dotenv.load();
    private static final String URL = dotenv.get("DB_URL");
    private static final String USERNAME = dotenv.get("DB_USERNAME");
    private static final String PASS = dotenv.get("DB_PASSWORD");

	private Database() {
		connect();
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

	public User addUser(User user) {
		String sql = "INSERT INTO users (Name, Surname, Email, Password) VALUES (?, ?, ?, ?)";
		try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
			stmt.setString(1, user.getName());
			stmt.setString(2, user.getSurname());
			stmt.setString(3, user.getEmail());
			stmt.setString(4, user.getHashedPassword());
			stmt.executeUpdate();

			return user;
		} catch (SQLException e) {
			return null;
		}
	}

	public void updateUser(User user) {
		String sql = "UPDATE users SET Name = ?, Surname = ?, Email = ?, Password = ? WHERE ID = ?";
		try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
			stmt.setString(1, user.getName());
			stmt.setString(2, user.getSurname());
			stmt.setString(3, user.getEmail());
			stmt.setString(4, user.getHashedPassword());
			stmt.setInt(5, user.getUserID());
			stmt.executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			showError();
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

	public User getUser(int id) {
		String sql = "Select * from users WHERE ID = ?";
		try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					return null;
				}

				return new User(
					rs.getInt("ID"),
					rs.getString("email"),
					rs.getString("name"),
					rs.getString("surname"),
					rs.getString("password"),
					false
				);
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showError();
			return null;
		}
	}

	public User getUser(String email) {
		String sql = "Select * from users WHERE email = ?";
		try (PreparedStatement stmt = myConn.prepareStatement(sql)) {
			stmt.setString(1, email);
			try (ResultSet rs = stmt.executeQuery()) {
				if (!rs.next()) {
					return null;
				}

				return new User(
					rs.getInt("ID"),
					rs.getString("email"),
					rs.getString("name"),
					rs.getString("surname"),
					rs.getString("password"),
					false
				);
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			showError();
			return null;
		}
	}

	public boolean addAdmin(int id, String password) {
		String sql = "INSERT INTO `admins`(`UserID`, `AdminPass`) VALUES (?, ?)";
		try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			pstmt.setString(2, password);
			pstmt.executeUpdate();
			return true;
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
			return false;
		}
	}

	public String getAdminPassword(int id) {
		String sql = "SELECT AdminPass FROM `admins` WHERE UserID = ?";
		try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				return rs.getString("AdminPass");
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	public boolean getIsAdmin(User user) {
		String sql = "SELECT * FROM `admins` WHERE UserID = ?";
		try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
			pstmt.setInt(1, user.getUserID());
			try (ResultSet rs = pstmt.executeQuery()) {
				return rs.next();
			} catch (SQLException e) {
				e.printStackTrace();
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean removeAdmin(int id) {
		String sql = "DELETE FROM `admins` WHERE UserID = ?";
		try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
			pstmt.setInt(1, id);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
			return false;
		}
	}

	public boolean addAccount(Account acc) {
		String sql = "INSERT INTO `accounts`(`Number`, `Owner`, `Type`, `Value`, `Currency`) VALUES ('0',?,?,?,?)";
		try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
			pstmt.setInt(1, acc.getOwnerID());
			pstmt.setString(2, acc.getAccountType().name());
			pstmt.setDouble(3, acc.getBalance().doubleValue());
			pstmt.setString(4, acc.getCurrencyCode());

			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateAccount(Account account) {
		String sql = "UPDATE accounts SET Value = ?, Type = ?, Currency = ? WHERE Number = ?";
		try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
			pstmt.setBigDecimal(1, account.getBalance());
			pstmt.setString(2, account.getAccountType().name());
			pstmt.setString(3, account.getCurrencyCode());
			pstmt.setInt(4, account.getAccountNumber());
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
			return false;
		}
	}

	public boolean updateAccounts(Account... accounts) {
		String sql = "UPDATE accounts SET Value = ?, Type = ?, Currency = ? WHERE Number = ?";
		try {
			myConn.setAutoCommit(false);
			try (PreparedStatement pstmt = myConn.prepareStatement(sql)) {
				for (Account account : accounts) {
					pstmt.setBigDecimal(1, account.getBalance());
					pstmt.setString(2, account.getAccountType().name());
					pstmt.setString(3, account.getCurrencyCode());
					pstmt.setInt(4, account.getAccountNumber());
					pstmt.addBatch();
				}
				pstmt.executeBatch();
				myConn.commit();
				return true;
			} catch (SQLException e) {
				myConn.rollback();
				showError();
				e.printStackTrace();
				return false;
			} finally {
				myConn.setAutoCommit(true);
			}
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
			return false;
		}
	}

	public boolean removeAccount(int number) {
		String sql = "DELETE FROM accounts WHERE Number = ?";
		try {
			java.sql.PreparedStatement stmt = myConn.prepareStatement(sql);
			stmt.setInt(1, number);
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			showError();
			e.printStackTrace();
			return false;
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
					int ownerID = rs.getInt("Owner");
					Account account = new Account(balance, currency, accountNumber, accountType, ownerID);
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

	public Account getAccount(int accountNumber) {
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
				int ownerID = rs.getInt("Owner");
				return new Account(balance, currency, number, accountType, ownerID);
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
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
