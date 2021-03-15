package trainingplans.utils;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class FileAccess {
	private final File file;
	private final String dialogContent;

	public FileAccess(String fileName, String dialogContent) {
		file = new File(fileName);
		this.dialogContent = dialogContent;
	}

	public void open() {
		if (Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().open(file);
			} catch (IllegalArgumentException ex) {
				ErrorMessage alert = new ErrorMessage(ex, "Öffnen fehlgeschlagen", dialogContent + " wurde nicht gefunden:\n" + ex.getMessage());
				alert.showMessage();
			} catch (IOException ex) {
				ErrorMessage alert = new ErrorMessage(ex, "Öffnen fehlgeschlagen", dialogContent + " konnte nicht geöffnet werden:\n" + ex.getMessage());
				alert.showMessage();
			}
		} else {
			Alert alert = new Alert(AlertType.ERROR, "Das Öffnen der Datei wird von Ihrem Betriebssystem nicht unterstützt.");
			alert.setHeaderText("Öffnen fehlgeschlagen");
			alert.showAndWait();
		}
	}
}
