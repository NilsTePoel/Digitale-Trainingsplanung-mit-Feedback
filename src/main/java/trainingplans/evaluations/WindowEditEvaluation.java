package trainingplans.evaluations;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import trainingplans.database.Database;
import trainingplans.database.LoadDegree;
import trainingplans.players.Player;

public class WindowEditEvaluation {
	private final Database db;
	private final ObservableList<Evaluation> evaluations;
	private final Player player;
	private final String searchQuery;
	private final Evaluation oldData;
	private TextField tfName;
	private ToggleGroup groupLoad;
	private TextField tfGoals;

	public WindowEditEvaluation(Database db, ObservableList<Evaluation> evaluations, Player player, String searchQuery, Evaluation oldData) {
		this.db = db;
		this.evaluations = evaluations;
		this.player = player;
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
		dialog.setTitle("Spielereinschätzung bearbeiten");
		Scene scene = new Scene(grid, 410, 200);
		dialog.setScene(scene);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(parent);
		dialog.setX(parent.getX() + parent.getWidth() / 2 - 205);
		dialog.setY(parent.getY() + parent.getHeight() / 2 - 100);
		dialog.setResizable(false);

		Text title = new Text("Geänderte Daten:");
		title.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		grid.add(title, 0, 0, 2, 1);

		Label lblName = new Label("Datum:");
		grid.add(lblName, 0, 1);

		tfName = new TextField();
		tfName.setText(oldData.getName());
		grid.add(tfName, 1, 1, 5, 1);

		Label lblLoad = new Label("Gesamtbelastung/-beanspruchung:");
		grid.add(lblLoad, 0, 2);

		groupLoad = new ToggleGroup();

		// Für jeden Belastungsgrad einen Radio-Button hinzufügen
		for (int i = 0; i < LoadDegree.values().length; i++) {
			RadioButton rbLoadDegree = new RadioButton(LoadDegree.valueOf(i).toString());
			rbLoadDegree.setUserData(LoadDegree.valueOf(i)); // Enum-Wert speichern
			rbLoadDegree.setToggleGroup(groupLoad);
			grid.add(rbLoadDegree, i + 1, 2);

			// Zuvor ausgewählten Radio-Button wieder auswählen
			if (rbLoadDegree.getUserData() == oldData.getLoad()) {
				rbLoadDegree.setSelected(true);
			}
		}

		Label lblGoals = new Label("Trainingsziele:");
		grid.add(lblGoals, 0, 3);

		tfGoals = new TextField();
		tfGoals.setText(oldData.getGoals());
		grid.add(tfGoals, 1, 3, 5, 1);

		Button btnEdit = new Button("Spielereinschätzung-Daten ändern");
		grid.add(btnEdit, 0, 4);

		dialog.show();

		tfGoals.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				updateEvaluation(dialog);
			}
		});

		btnEdit.setOnAction(e -> updateEvaluation(dialog));
	}

	private void updateEvaluation(Stage dialog) {
		if (tfName.getText().isBlank()) {
			Alert alert = new Alert(AlertType.ERROR, "Es wurden noch nicht alle notwendigen Felder ausgefüllt.");
			alert.setHeaderText("Daten unvollständig");
			alert.showAndWait();
		} else {
			db.updateEvaluation(new Evaluation(oldData.getID(), tfName.getText(), (LoadDegree) groupLoad.getSelectedToggle().getUserData(), tfGoals.getText()));
			db.searchTableEvaluations(evaluations, player, searchQuery);
			dialog.close();
		}
	}
}
