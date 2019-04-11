package Accounts;

import java.math.BigDecimal;

public class KreditaKonts extends AccountTemplate {
	// Kredīta -> par katru izejošo summu -5%
	
	
	public KreditaKonts (float money) {
		initialize();
		setMoney(money);
	}
	public KreditaKonts() {
		initialize();
	}

	@Override
	public void sendMoney(float money) {
		BigDecimal decMoney = new BigDecimal(Float.toString(calculatePercentage(money,95)));
		this.setMoney(getMoney().subtract(decMoney));
	}

	@Override
	public void receiveMoney(float money) {
		BigDecimal decMoney = new BigDecimal(Float.toString(money));
		this.setMoney(this.getMoney().add(decMoney));
	}
	
	
	private void initialize() {
		setMoney(0);
		setMoneyType("EUR");
		setAccountType(KREDITA_KONTS);
	}
	

}
