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

    public Account sendMoney(Account fromAccount, int toAccount, BigDecimal amount) {
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        Account toAcc = database.getAccount(toAccount);

        if (fromAccount.getOwnerID() == toAcc.getOwnerID()) {
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAcc.setBalance(toAcc.getBalance().add(amount));
            database.updateAccounts(fromAccount, toAcc);
            return toAcc;
        } else {
            //Get transfer strategy
        }

        return null;
    }

    public void addMoney(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        String accountOwner = userService.getCurrentUser().getName();
        database.updateAccount(account);
        Database.log(accountOwner, "added " + amount + " to account " + account.getAccountNumber());
    }
}