package Model.Accounts;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import Model.Database;


public class Konti {
	ArrayList<KreditaKonts> kd = new ArrayList<KreditaKonts>();
	ArrayList<AlgasKonts> ak = new ArrayList<AlgasKonts>();
	ArrayList<NoguldijumaKonts> nk = new ArrayList<NoguldijumaKonts>();

	public Konti() {

	}

	public void addAccount(AccountTemplate acc) {
		if (acc.getAccountType() == AccountTemplate.ALGAS_KONTS) {
			//Make it so that it adds to a array list later
			ak.add((AlgasKonts)acc.returnSelf());
		}

		if (acc.getAccountType() == AccountTemplate.KREDITA_KONTS) {
			kd.add((KreditaKonts)acc.returnSelf());
		}

		if (acc.getAccountType() == AccountTemplate.NOGULDIJUMA_KONTS) {
			nk.add((NoguldijumaKonts)acc.returnSelf());
		}
	}

	//Not the best ieda using result set here.
	public void addAccounts(ResultSet rs) {
		AccountTemplate acc;
		try {
			while(rs.next()) {
			acc = Database.createAccountBasedOnType(rs.getInt("Type"));
			//Default will be EUR for now because no currency conversion
			acc.setAll(rs.getFloat("Value"),rs.getString("Currency"),rs.getInt("Type"),rs.getInt("Number"));
			addAccount(acc);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public Konti getAccounts() {
		return this;
	}

	public ArrayList<AlgasKonts> getAlgasKonti() {
		return ak;
	}

	public ArrayList<KreditaKonts> getKreditaKonti() {
		return kd;
	}

	public ArrayList<NoguldijumaKonts> getNoguldijumaKonti() {
		return nk;
	}
}
