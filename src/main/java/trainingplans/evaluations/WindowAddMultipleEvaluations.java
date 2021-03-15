package trainingplans.evaluations;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import trainingplans.database.Database;
import trainingplans.database.LoadDegree;
import trainingplans.players.Player;

public class WindowAddMultipleEvaluations {
	private final Database db;
	private final List<Player> players;
	private final List<ToggleGroup> groups = new ArrayList<>();
	private final List<TextField> goals = new ArrayList<>();
	private TextField tfName;

	public WindowAddMultipleEvaluations(Database db, List<Player> players) {
		this.db = db;
		this.players = players;
	}

	public void open(Stage parent) {
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));

		ScrollPane scrollPane = new ScrollPane(); // Scrollbalken, wenn viele Einträge vorhanden sind
		scrollPane.setContent(grid);

		Stage dialog = new Stage();
		dialog.setTitle("Mehrere Spielereinschätzungen hinzufügen");
		Scene scene = new Scene(scrollPane, 580, 500);
		dialog.setScene(scene);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(parent);
		dialog.setX(parent.getX() + parent.getWidth() / 2 - 290);
		dialog.setY(parent.getY() + parent.getHeight() / 2 - 250);

		Text title = new Text("Spielereinschätzungen:");
		title.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		grid.add(title, 0, 0, 2, 1);

		Label lblName = new Label("Datum:");
		grid.add(lblName, 0, 1);

		tfName = new TextField();
		tfName.requestFocus();
		grid.add(tfName, 1, 1, 6, 1);

		for (int i = 0; i < players.size(); i++) {
			int position = i + 2;

			Label lblPlayerName = new Label(players.get(i).getName());
			grid.add(lblPlayerName, 0, position);

			ToggleGroup groupLoad = new ToggleGroup();
			groups.add(groupLoad);

			// Für jeden Belastungsgrad einen Radio-Button hinzufügen
			for (int j = 0; j < LoadDegree.values().length; j++) {
				RadioButton rbLoadDegree = new RadioButton(LoadDegree.valueOf(j).toString());
				rbLoadDegree.setUserData(LoadDegree.valueOf(j)); // Enum-Wert speichern
				rbLoadDegree.setToggleGroup(groupLoad);
				grid.add(rbLoadDegree, j + 1, position);
			}

			TextField tfGoals = new TextField();
			tfGoals.setPromptText("Trainingsziele");
			goals.add(tfGoals);
			grid.add(tfGoals, 6, position);
		}

		Button btnAdd = new Button("Spielereinschätzungen hinzufügen");
		grid.add(btnAdd, 0, players.size() + 2);

		dialog.show();

		btnAdd.setOnAction(e -> insertEvaluations(dialog));
	}

	// Eintrag für jeden Spieler anlegen (sofern ein Belastungsgrad eingetragen
	// wurde)
	private void insertEvaluations(Stage dialog) {
		if (tfName.getText().isBlank()) {
			Alert alert = new Alert(AlertType.ERROR, "Es wurde noch kein Datum eingegeben.");
			alert.setHeaderText("Datum fehlt");
			alert.showAndWait();
		} else {
			for (int i = 0; i < players.size(); i++) {
				String playerGoal = goals.get(i).getText();
				Toggle loadDegreeToggle = groups.get(i).getSelectedToggle();

				if (loadDegreeToggle != null) {
					LoadDegree playerLoadDegree = (LoadDegree) loadDegreeToggle.getUserData();

					db.insertEvaluation(new Evaluation(tfName.getText(), playerLoadDegree, playerGoal), players.get(i));
				}
			}

			dialog.close();
		}
	}
}