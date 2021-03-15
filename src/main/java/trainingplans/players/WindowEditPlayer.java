package trainingplans.players;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import trainingplans.database.Database;

public class WindowEditPlayer {
	private final Database db;
	private final ObservableList<Player> players;
	private final String searchQuery;
	private final Player oldData;
	private TextField tfName;

	public WindowEditPlayer(Database db, ObservableList<Player> players, String searchQuery, Player oldData) {
		this.db = db;
		this.players = players;
		this.searchQuery = searchQuery;
		this.oldData = oldData;
	}

	public void open(Stage parent) {
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));

		Stage dialog = new Stage();
		dialog.setTitle("Spieler bearbeiten");
		Scene scene = new Scene(grid, 400, 150);
		dialog.setScene(scene);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(parent);
		dialog.setX(parent.getX() + parent.getWidth() / 2 - 200);
		dialog.setY(parent.getY() + parent.getHeight() / 2 - 75);
		dialog.setResizable(false);

		Text title = new Text("Geänderte Daten:");
		title.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		grid.add(title, 0, 0, 2, 1);

		Label lblName = new Label("Name:");
		grid.add(lblName, 0, 1);

		tfName = new TextField();
		tfName.setText(oldData.getName());
		grid.add(tfName, 1, 1);

		Button btnEdit = new Button("Spieler-Daten ändern");
		grid.add(btnEdit, 0, 2);

		dialog.show();

		tfName.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				updatePlayer(dialog);
			}
		});

		btnEdit.setOnAction(e -> updatePlayer(dialog));
	}

	private void updatePlayer(Stage dialog) {
		if (tfName.getText().isBlank()) {
			Alert alert = new Alert(AlertType.ERROR, "Es wurde noch kein Name eingegeben.");
			alert.setHeaderText("Name fehlt");
			alert.showAndWait();
		} else {
			db.updatePlayer(new Player(oldData.getID(), tfName.getText()));
			db.searchTablePlayers(players, searchQuery);
			dialog.close();
		}
	}
}
