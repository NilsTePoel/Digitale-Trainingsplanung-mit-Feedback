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

public class WindowAddPlayer {
	private final Database db;
	private final ObservableList<Player> players;
	private final String searchQuery;
	private TextField tfName;

	public WindowAddPlayer(Database db, ObservableList<Player> players, String searchQuery) {
		this.db = db;
		this.players = players;
		this.searchQuery = searchQuery;
	}

	public void open(Stage parent) {
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));

		Stage dialog = new Stage();
		dialog.setTitle("Spieler hinzufügen");
		Scene scene = new Scene(grid, 400, 150);
		dialog.setScene(scene);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(parent);
		dialog.setX(parent.getX() + parent.getWidth() / 2 - 200);
		dialog.setY(parent.getY() + parent.getHeight() / 2 - 75);
		dialog.setResizable(false);

		Text title = new Text("Neuer Spieler:");
		title.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		grid.add(title, 0, 0, 2, 1);

		Label lblName = new Label("Name:");
		grid.add(lblName, 0, 1);

		tfName = new TextField();
		grid.add(tfName, 1, 1);

		Button btnAdd = new Button("Spieler hinzufügen");
		grid.add(btnAdd, 0, 2);

		dialog.show();

		tfName.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				insertPlayer(dialog);
			}
		});

		btnAdd.setOnAction(e -> insertPlayer(dialog));
	}

	private void insertPlayer(Stage dialog) {
		if (tfName.getText().isBlank()) {
			Alert alert = new Alert(AlertType.ERROR, "Es wurde noch kein Name eingegeben.");
			alert.setHeaderText("Name fehlt");
			alert.showAndWait();
		} else {
			db.insertPlayer(new Player(tfName.getText()));
			db.searchTablePlayers(players, searchQuery);
			dialog.close();
		}
	}
}
