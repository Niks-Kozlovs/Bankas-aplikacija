package Accounts;

import java.math.BigDecimal;

public class AlgasKonts extends AccountTemplate {
	
	AlgasKonts(float money) {
		initialize();
		setMoney(money);
	}
	
	public AlgasKonts() {
		initialize();
	}

	@Override
	public void sendMoney(float money) {
		BigDecimal decMoney = new BigDecimal(Float.toString(money));
		this.setMoney(this.getMoney().subtract(decMoney));
		
	}

	@Override
	public void receiveMoney(float money) {
		BigDecimal decMoney = new BigDecimal(Float.toString(money));
		this.setMoney(this.getMoney().add(decMoney));
		
	}
	
	private void initialize() {
		setMoney(0);
		setMoneyType("USD");
		setAccountType(ALGAS_KONTS);
	}


}
