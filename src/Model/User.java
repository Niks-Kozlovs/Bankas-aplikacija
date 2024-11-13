package Model;

public class User {
	int userID = -1; //Not set
	String email = null;
	String name = null;
	String surname = null;
	boolean isAdmin = false;

	public User() {

	}

	public User(int userID, String email, String name, String surname, boolean isAdmin) {
		setAll(userID, email, name, surname, isAdmin);
	}

	public User(int userID, String email, String name, String surname) {
		this(userID, email, name, surname, false);
	}

	public void setAll(int userID,String email, String name, String surname) {
		setUserID(userID);
		setEmail(email);
		setName(name);
		setSurname(surname);
	}

	public void setAll(int userID, String email, String name, String surname, boolean isAdmin) {
		setAll(userID,email,name,surname);
		setIsAdmin(isAdmin);

	}
	public int getUserID() {
		return userID;
	}

	public void setUserID(int userID) {
		this.userID = userID;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public void setIsAdmin (boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public boolean getIsAdmin () {
		return isAdmin;
	}

}
