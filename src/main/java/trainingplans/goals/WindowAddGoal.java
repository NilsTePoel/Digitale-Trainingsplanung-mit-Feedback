package trainingplans.goals;

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

public class WindowAddGoal {
	private final Database db;
	private final ObservableList<Goal> goals;
	private final String searchQuery;
	private TextField tfAbbreviation;
	private TextField tfName;

	public WindowAddGoal(Database db, ObservableList<Goal> goals, String searchQuery) {
		this.db = db;
		this.goals = goals;
		this.searchQuery = searchQuery;
	}

	public void open(Stage parent) {
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));

		Stage dialog = new Stage();
		dialog.setTitle("Trainingsziel hinzufügen");
		Scene scene = new Scene(grid, 400, 200);
		dialog.setScene(scene);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(parent);
		dialog.setX(parent.getX() + parent.getWidth() / 2 - 200);
		dialog.setY(parent.getY() + parent.getHeight() / 2 - 100);
		dialog.setResizable(false);

		Text title = new Text("Neues Trainingsziel:");
		title.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		grid.add(title, 0, 0, 2, 1);

		Label lblAbbreviation = new Label("Abkürzung:");
		grid.add(lblAbbreviation, 0, 1);

		tfAbbreviation = new TextField();
		grid.add(tfAbbreviation, 1, 1);

		Label lblName = new Label("Name:");
		grid.add(lblName, 0, 2);

		tfName = new TextField();
		grid.add(tfName, 1, 2);

		Button btnAdd = new Button("Trainingsziel hinzufügen");
		grid.add(btnAdd, 0, 3);

		dialog.show();

		tfAbbreviation.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				tfName.requestFocus();
			}
		});

		tfName.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				insertGoal(dialog);
			}
		});

		btnAdd.setOnAction(e -> insertGoal(dialog));
	}

	private void insertGoal(Stage dialog) {
		if (tfAbbreviation.getText().isBlank() || tfName.getText().isBlank()) {
			Alert alert = new Alert(AlertType.ERROR, "Es wurden noch nicht alle Felder ausgefüllt.");
			alert.setHeaderText("Daten unvollständig");
			alert.showAndWait();
		} else {
			db.insertGoal(new Goal(tfAbbreviation.getText(), tfName.getText()));
			db.searchTableGoals(goals, searchQuery);
			dialog.close();
		}
	}
}