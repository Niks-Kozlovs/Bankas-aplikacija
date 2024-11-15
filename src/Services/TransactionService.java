package Services;

import java.math.BigDecimal;

import Model.Accounts.Account;

public class TransactionService {
    private Database database;
    private static UserService userService = UserService.getInstance();;
    private static TransactionService instance;

    private TransactionService() {
        this.database = Database.getInstance();
    }

    public static TransactionService getInstance() {
        if (instance == null) {
            instance = new TransactionService();
        }
        return instance;
    }

    public float calculatePercentage(float value, int percentage) {
		float newValue = ((float) value * ((float) percentage / 100));
		return newValue;
	}

    public void sendMoney(Account fromAccount, Account toAccount, BigDecimal amount) {
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
        toAccount.setBalance(toAccount.getBalance().add(amount));
        //TODO: Add transaction to database
    }

    public void addMoney(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        String accountOwner = userService.getCurrentUser().getName();
        database.updateAccount(account);
        Database.log(accountOwner, "added " + amount + " to account " + account.getAccountNumber());
    }
}