package Accounts;

import java.math.BigDecimal;

public class NoguldijumaKonts extends AccountTemplate {
	//Noguldījuma -> par katru ien�?košo summu +5%
	
	NoguldijumaKonts(float money) {
		initialize();
		setMoney(money);
	}
	
	public NoguldijumaKonts() {
		initialize();
	}

	@Override
	public void sendMoney(float money) {
		BigDecimal decMoney = new BigDecimal(Float.toString(money));
		setMoney(getMoney().subtract(decMoney));
		
	}

	@Override
	public void receiveMoney(float money) {
		BigDecimal decMoney = new BigDecimal(Float.toString(calculatePercentage(money,105)));
		setMoney(getMoney().add(decMoney));
		
	}
	
	private void initialize() {
		setMoney(0);
		setMoneyType("EUR");
		setAccountType(NOGULDIJUMA_KONTS);
	}

}
