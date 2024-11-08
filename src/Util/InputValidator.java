package Util;

public class InputValidator {

	private static final char[] specialCharactersLocalPart = { '!', '#', '$', '%', '&', '*', '+', '-', '/', '=', '?',
			'^', '`', '{', '|', '}', '~' };

	public static String capitalizeFirstLetter(String text) {

		return text.substring(0, 1).toUpperCase() + text.substring(1, text.length()).toLowerCase();
	}

	//Realisticly I would ignore the checks and let the user do whatever. In a real bank it would be confirmed by a human
	public static boolean isValidNameSurname(String text) {
		if (text.equals("")) {
			return false;
		}
		char[] textArray = text.toCharArray();
		for (int i = 0; i < textArray.length; i++) {
			if (!isALetter(textArray[i])) {
				return false;
			}
		}
		return true;
	}

	// Realisticly I would ignore the checks and just send a confirmation email
	public static boolean checkEmail(String email) {
		if (email.equals("")) {
			return false;
		}
		if (email.indexOf('@') == -1)
			return false;
		// If more than one @ symbol
		if (email.length() - email.replace("@", "").length() > 1) {
			return false;
		}

		// Email is made of local-part and case insensitive domain
		char[] localPart = email.split("@", 2)[0].toCharArray();
		char[] domain = email.split("@", 2)[1].toCharArray();

		if (localPart.length > 64 || domain.length > 255) {
			return false;
		}

		// Epasta nedrikst punktam atrasties sakuma vai beigas
		if (localPart[0] == '.' || localPart[localPart.length - 1] == '.')
			return false;

		if (domain[0] == '-' || domain[domain.length - 1] == '-')
			return false;

		if (!checkLocalPart(localPart))
			return false;

		if (!checkDomain(domain))
			return false;

		return true;
	}

	private static boolean checkDomain(char[] domain) {
		for (int i = 0; i < domain.length; i++) {
			// Nevar but double dot
			if (domain[i] == '.' && domain[i + 1] == '.') {
				return false;
			} else if (!(isALetter(domain[i]) || isANumber(domain[i])) && !(domain[i] == '.')) {
				return false;
			}
		}
		return true;
	}

	private static boolean checkLocalPart(char[] localPart) {
		for (int i = 0; i < localPart.length - 1; i++) {

			if (!(isALetter(localPart[i]) || isANumber(localPart[i])
					|| isSpecialCharacterException(localPart[i], specialCharactersLocalPart)))
				return false;
		}
		return true;
	}

	private static boolean isSpecialCharacterException(char c, char[] specialCharacters) {

		for (int i = 0; i < specialCharacters.length - 1; i++) {
			if (c == specialCharacters[i]) {
				return true;
			}
		}
		return false;
	}

	private static boolean isALetter(char c) {
		if ('a' <= c && c <= 'z') {
			return true;
		} else if ('A' <= c && c <= 'Z') {
			return true;
		} else {
			return false;
		}
	}

	private static boolean isANumber(char c) {
		if ('0' <= c && c <= '9') {
			return true;
		} else {
			return false;
		}
	}

	public static boolean checkInteger(String int1) {
		try {
			Integer.parseInt(int1);
			return true;

		} catch (NumberFormatException e) {
			return false;
		}
	}

	public static boolean checkFloat(String float1) {
		try {
			Float.parseFloat(float1);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

}
