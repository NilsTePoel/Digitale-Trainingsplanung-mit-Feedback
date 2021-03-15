package trainingplans.utils;

import java.io.PrintWriter;
import java.io.StringWriter;

import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

// Fehlermeldung, die es erlaubt, zusätzliche Infos in die Zwischenablage zu kopieren
public class ErrorMessage extends Alert {
	private static final ButtonType COPY_ERROR_MESSAGE = new ButtonType("Fehlermeldung kopieren", ButtonData.HELP_2);
	private final Exception ex;

	public ErrorMessage(Exception ex, String title, String message) {
		super(AlertType.ERROR, message, COPY_ERROR_MESSAGE, ButtonType.OK);
		this.ex = ex;
		setHeaderText(title);

		// Fehlermeldung in die Zwischenablage kopieren und das Schließen des Dialogs
		// verhindern
		Button btnCopyErrorMessage = (Button) getDialogPane().lookupButton(COPY_ERROR_MESSAGE);
		btnCopyErrorMessage.addEventFilter(ActionEvent.ACTION, event -> {
			Clipboard clipboard = Clipboard.getSystemClipboard();
			ClipboardContent errorMessage = new ClipboardContent();

			// Stack Trace in eine Zeichenkette umwandeln
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ex.printStackTrace(pw);
			String stackTrace = sw.toString();

			errorMessage.putString(message + "\n" + stackTrace);
			clipboard.setContent(errorMessage);
			event.consume();
		});
	}

	// Fehlermeldung mit Standardtext
	public ErrorMessage(Exception ex) {
		this(ex, "Datenbankabfrage fehlgeschlagen", "Bei der Datenbankabfrage ist ein Fehler aufgetreten:\n" + ex.getMessage());
	}

	public void showMessage() {
		ex.printStackTrace();
		showAndWait();
	}
}
