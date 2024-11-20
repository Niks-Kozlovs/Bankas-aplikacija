package Services;

import java.math.BigDecimal;

import Model.Accounts.Account;
import Model.Accounts.AccountType;

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

    public Account sendMoney(Account fromAccount, int toAccount, BigDecimal amount) {
        if (fromAccount.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient funds");
        }

        String accountOwner = userService.getCurrentUser().getName();
        Account toAcc = database.getAccount(toAccount);

        if (fromAccount.getOwnerID() == toAcc.getOwnerID()) {
            fromAccount.setBalance(fromAccount.getBalance().subtract(amount));
            toAcc.setBalance(toAcc.getBalance().add(amount));
            database.updateAccounts(fromAccount, toAcc);
            Database.log(accountOwner, "sent " + amount + " to account " + toAccount);
            return toAcc;
        }

        if (fromAccount.getAccountType() == AccountType.KREDITA_KONTS) {
        	// Kredīta -> par katru izejošo summu -5%
            BigDecimal fromAmount = amount.multiply(new BigDecimal("0.95"));
            fromAccount.setBalance(fromAccount.getBalance().subtract(fromAmount));
        }

        if (toAcc.getAccountType() == AccountType.NOGULDIJUMA_KONTS) {
        	// Noguldījuma -> par katru ienākošo summu +5%
            BigDecimal toAmount = amount.multiply(new BigDecimal("1.05"));
            toAcc.setBalance(toAcc.getBalance().add(toAmount));
        }

        database.updateAccounts(fromAccount, toAcc);
        Database.log(accountOwner, "sent " + amount + " from " + fromAccount + " to account " + toAccount);
        return null;
    }

    public void addMoney(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        String accountOwner = userService.getCurrentUser().getName();
        database.updateAccount(account);
        Database.log(accountOwner, "added " + amount + " to account " + account.getAccountNumber());
    }
}