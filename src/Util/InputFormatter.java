package Util;

import javafx.scene.control.TextFormatter;

public class InputFormatter {

	public static TextFormatter<String> getOnlyDigitsFormatter() {
		return new TextFormatter<>(change -> {
			if (change.getControlNewText().matches("^[0-9]*$")) {
				return change;
			}
			return null;
		});
    }

	public static TextFormatter<String> getOnlyDoubleTextFormatter() {
		return new TextFormatter<>(change -> {
			if (change.getControlNewText().matches("^[0-9]*\\.?[0-9]{0,2}$")) {
				return change;
			}
			return null;
		});
	}

	public static TextFormatter<String> getEmailFormatter() {
		return new TextFormatter<>(change -> {
			if (change.getControlNewText().matches("^[a-zA-Z0-9@.]*$")) {
				return change;
			}
			return null;
		});
	}

}
