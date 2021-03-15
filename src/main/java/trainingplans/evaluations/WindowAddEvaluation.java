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

public class WindowAddEvaluation {
	private final Database db;
	private final ObservableList<Evaluation> evaluations;
	private final Player player;
	private final String searchQuery;
	private TextField tfName;
	private ToggleGroup groupLoad;
	private TextField tfGoals;

	public WindowAddEvaluation(Database db, ObservableList<Evaluation> evaluations, Player player, String searchQuery) {
		this.db = db;
		this.evaluations = evaluations;
		this.player = player;
		this.searchQuery = searchQuery;
	}

	public void open(Stage parent) {
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));

		Stage dialog = new Stage();
		dialog.setTitle("Spielereinschätzung hinzufügen");
		Scene scene = new Scene(grid, 410, 200);
		dialog.setScene(scene);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(parent);
		dialog.setX(parent.getX() + parent.getWidth() / 2 - 205);
		dialog.setY(parent.getY() + parent.getHeight() / 2 - 100);
		dialog.setResizable(false);

		Text title = new Text("Neue Spielereinschätzung:");
		title.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		grid.add(title, 0, 0, 2, 1);

		Label lblName = new Label("Datum:");
		grid.add(lblName, 0, 1);

		tfName = new TextField();
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
		}

		Label lblGoals = new Label("Trainingsziele:");
		grid.add(lblGoals, 0, 3);

		tfGoals = new TextField();
		grid.add(tfGoals, 1, 3, 5, 1);

		Button btnAdd = new Button("Spielereinschätzung hinzufügen");
		grid.add(btnAdd, 0, 4);

		dialog.show();

		tfGoals.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				insertEvaluation(dialog);
			}
		});

		btnAdd.setOnAction(e -> insertEvaluation(dialog));
	}

	private void insertEvaluation(Stage dialog) {
		if (tfName.getText().isBlank() || groupLoad.getSelectedToggle() == null) {
			Alert alert = new Alert(AlertType.ERROR, "Es wurden noch nicht alle notwendigen Felder ausgefüllt.");
			alert.setHeaderText("Daten unvollständig");
			alert.showAndWait();
		} else {
			db.insertEvaluation(new Evaluation(tfName.getText(), (LoadDegree) groupLoad.getSelectedToggle().getUserData(), tfGoals.getText()), player);
			db.searchTableEvaluations(evaluations, player, searchQuery);
			dialog.close();
		}
	}
}