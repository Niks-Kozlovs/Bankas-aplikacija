package Model.Accounts;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;

public class Account {
	private int accountNumber;
	private BigDecimal balance = new BigDecimal("0");
	private Currency moneyType = Currency.getInstance("EUR");
	private AccountType accountType;
	private int ownerID;

	public Account(BigDecimal balance, Currency currency, int accountNumber, AccountType accountType, int ownerID) {
		setBalance(balance);
		setMoneyType(currency.getCurrencyCode());
		setAccountNumber(accountNumber);
		setAccountType(accountType);
		setOwnerID(ownerID);
	};

	public void setAccountType(AccountType accountType) {
		this.accountType = accountType;
	}

	public AccountType getAccountType() {
		return accountType;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(double money) {
		this.balance = BigDecimal.valueOf(money).setScale(2, RoundingMode.HALF_EVEN);
	}

	public void setBalance(BigDecimal money) {
		this.balance = money.setScale(2, RoundingMode.HALF_EVEN);
	}

	public String getMoneyType() {
		return moneyType.getSymbol();
	}

	public void setMoneyType(String moneyType) {
		this.moneyType = Currency.getInstance(moneyType);
	}

	public int getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(int accountNumber) {
		this.accountNumber = accountNumber;
	}

	public String getAccountName() {
		return accountType.getName();
	}

	public int getOwnerID() {
		return ownerID;
	}

	public void setOwnerID(int ownerID) {
		this.ownerID = ownerID;
	}

	public void printInfo() {
		System.out.printf(
			"Balance: %s%nMoney type:%s%nAccount type: %d%n Owner: %s%n",
			balance.toString(),
			moneyType.getSymbol(),
			this.getClass().getName(),
			this.getOwnerID()
		);
	}
}
