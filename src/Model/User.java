package Model;

public class User {
	int userID = -1; //Not set
	String email = null;
	String name = null;
	String surname = null;
	String hashedPassword = null;
	boolean isAdmin = false;

	public User(int id, String email, String name, String surname, String hashedPassword, boolean isAdmin) {
		this.userID = id;
		this.email = email;
		this.name = name;
		this.surname = surname;
		this.hashedPassword = hashedPassword;
		this.isAdmin = isAdmin;
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

	public String getHashedPassword() {
		return hashedPassword;
	}

	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}

}
