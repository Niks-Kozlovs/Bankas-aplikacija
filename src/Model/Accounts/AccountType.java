package Model.Accounts;

public enum AccountType {
	ALGAS_KONTS("Algas Konts"), KREDITA_KONTS("Kredita Konts"), NOGULDIJUMA_KONTS("Noguldijuma Konts");

	private final String name;

	AccountType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}