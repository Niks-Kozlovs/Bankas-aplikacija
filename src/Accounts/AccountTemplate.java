package Accounts;

import java.math.BigDecimal;
//Too bat that it doesnt convert money
import java.util.Currency;

public abstract class AccountTemplate {
	// Tiek izmantots BigDecimal prieks precizitates
	private BigDecimal money = new BigDecimal("0");
	private Currency moneyType = Currency.getInstance("EUR");
	private int accountType;
	private String accountTypeString = null;
	private int accountNumber;

	public final static int ALGAS_KONTS = 1;
	public final static int NOGULDIJUMA_KONTS = 2;
	public final static int KREDITA_KONTS = 3;

	public AccountTemplate() {
	};

	public AccountTemplate(float money) {
		setMoney(money);
	}
	
	public void setAll(float money, String moneyType, int accountType, int accountNumber) {
		setMoney(money);
		setMoneyType(moneyType);
		setAccountType(accountType);
		setAccountTypeString(accountType);
		setAccountNumber(accountNumber);
	}
	//TODO Add connect with database to send and receive money
	public abstract void sendMoney(float money);

	public abstract void receiveMoney(float money);
	
	public void addMoney(float money) {
		setMoney(getMoney().floatValue() + money);
	}
	
	public void removeMoney(float money) {
		//Makes the value negative for removing the money
		money = money * -1;
		addMoney(money);
	}
	
	public AccountTemplate returnSelf() {
		return this;
	};
	public void printInfo() {
		System.out.printf("Money: %s%nMoney type:%s%nAccount type: %d%n", money.toString(), moneyType.getSymbol(),
				accountType);
	}

	// For NoguldijumaKonts and KreditaKonts
	public float calculatePercentage(float value, int percentage) {
		float newValue = ((float) value * ((float) percentage / 100));
		return newValue;
	}

	public BigDecimal getMoney() {
		return money;
	}

	// Round up for customers
	public void setMoney(float money) {
		String moneyString = Float.toString(money);
		this.money = new BigDecimal(moneyString).setScale(2, BigDecimal.ROUND_UP);
	}


	public void setMoney(BigDecimal money) {
		this.money = money.setScale(2, BigDecimal.ROUND_UP);
	}

	public String getMoneyType() {
		return moneyType.getSymbol();
	}

	public void setMoneyType(String moneyType) {
		this.moneyType = Currency.getInstance(moneyType);
	}

	public int getAccountType() {
		return accountType;
	}

	public void setAccountType(int accountType) {
		this.accountType = accountType;
	}

	public int getAccountNumber() {
		return accountNumber;
	}

	public void setAccountNumber(int accountNumber) {
		this.accountNumber = accountNumber;
	}
	
	public void setAccountTypeString (int accountType) {
		if (accountType == ALGAS_KONTS) {
			accountTypeString = "Algas konts";
		}
		if (accountType == NOGULDIJUMA_KONTS) {
			accountTypeString = "Noguldijuma konts";
		}
		if (accountType == KREDITA_KONTS) {
			accountTypeString = "Kredita konts";
		}
	}
	
	public String getAccountTypeString() {
		return accountTypeString;
	}
}
