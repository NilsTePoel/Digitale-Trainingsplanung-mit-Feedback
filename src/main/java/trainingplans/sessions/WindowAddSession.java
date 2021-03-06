package trainingplans.sessions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import trainingplans.database.Database;
import trainingplans.database.LoadDegree;

public class WindowAddSession {
	private final Database db;
	private final ObservableList<Session> sessions;
	private final String searchQuery;
	private final ArrayList<String> goals;
	private TextField tfName;
	private TextField tfTopic;
	private ToggleGroup groupScope;
	private ToggleGroup groupIntensity;
	private ToggleGroup groupPressure;
	private ToggleGroup groupAttention;
	private ToggleGroup groupTotal;
	private MenuButton mbGoals;
	private String plan;

	public WindowAddSession(Database db, ObservableList<Session> sessions, String searchQuery, ArrayList<String> goals) {
		this.db = db;
		this.sessions = sessions;
		this.searchQuery = searchQuery;
		this.goals = goals;
	}

	public void open(Stage parent) {
		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));

		Stage dialog = new Stage();
		dialog.setTitle("Trainingseinheit hinzufügen");
		Scene scene = new Scene(grid, 410, 400);
		dialog.setScene(scene);
		dialog.initModality(Modality.WINDOW_MODAL);
		dialog.initOwner(parent);
		dialog.setX(parent.getX() + parent.getWidth() / 2 - 205);
		dialog.setY(parent.getY() + parent.getHeight() / 2 - 200);
		dialog.setResizable(false);

		Text title = new Text("Neue Trainingseinheit:");
		title.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		grid.add(title, 0, 0, 2, 1);

		Label lblName = new Label("Datum:");
		grid.add(lblName, 0, 1);

		tfName = new TextField();
		grid.add(tfName, 1, 1);

		Label lblTopic = new Label("Trainingsschwerpunkt:");
		grid.add(lblTopic, 0, 2);

		tfTopic = new TextField();
		grid.add(tfTopic, 1, 2);

		GridPane gridLoad = new GridPane();
		gridLoad.setHgap(10);

		Label lblScope = new Label("Umfang:");
		gridLoad.add(lblScope, 0, 0);

		groupScope = new ToggleGroup();
		addRadioButtons(gridLoad, 0, groupScope);

		Label lblIntensity = new Label("Intensität:");
		gridLoad.add(lblIntensity, 0, 1);

		groupIntensity = new ToggleGroup();
		addRadioButtons(gridLoad, 1, groupIntensity);

		Label lblPressure = new Label("Druckbedingungen:");
		gridLoad.add(lblPressure, 0, 2);

		groupPressure = new ToggleGroup();
		addRadioButtons(gridLoad, 2, groupPressure);

		Label lblAttention = new Label("Aufmerksamkeit/Konzentration:");
		gridLoad.add(lblAttention, 0, 3);

		groupAttention = new ToggleGroup();
		addRadioButtons(gridLoad, 3, groupAttention);

		Label lblTotal = new Label("Gesamt:");
		gridLoad.add(lblTotal, 0, 4);

		groupTotal = new ToggleGroup();
		addRadioButtons(gridLoad, 4, groupTotal);

		TitledPane tpLoad = new TitledPane("Belastung und Beanspruchung", gridLoad);
		tpLoad.setCollapsible(false);
		grid.add(tpLoad, 0, 3, 2, 1);

		Label lblGoals = new Label("Trainingsziele:");
		grid.add(lblGoals, 0, 4);

		mbGoals = new MenuButton();
		mbGoals.setMaxWidth(Double.MAX_VALUE);
		grid.add(mbGoals, 1, 4);

		GridPane gridPlan = new GridPane();
		gridPlan.setHgap(10);

		Label lblFileName = new Label("Keine Datei ausgewählt");
		gridPlan.add(lblFileName, 0, 0);

		Button btnPlan = new Button("Trainingsplan anfügen");
		btnPlan.setMaxWidth(Double.MAX_VALUE);
		gridPlan.add(btnPlan, 1, 0);

		TitledPane tpPlan = new TitledPane("Trainingsplan", gridPlan);
		tpPlan.setCollapsible(false);
		grid.add(tpPlan, 0, 5, 2, 1);

		Button btnAdd = new Button("Trainingseinheit hinzufügen");
		grid.add(btnAdd, 0, 6);

		dialog.show();

		// Alle Ziele zum Menü hinzufügen
		for (String goal : goals) {
			mbGoals.getItems().add(new CheckMenuItem(goal));
		}

		btnPlan.setOnAction(e -> {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Trainingsplan anfügen");
			File planFile = fileChooser.showOpenDialog(dialog);
			if (planFile != null) {
				plan = planFile.getPath();
				lblFileName.setText(planFile.getName());
			}
		});

		// Label bei jeder Änderung der Ziele anpassen
		for (MenuItem item : mbGoals.getItems()) {
			item.setOnAction(e -> {
				List<String> selectedGoals = getSelectedItems();
				if (selectedGoals.isEmpty()) {
					mbGoals.setText("");
				} else {
					mbGoals.setText(selectedGoals.toString().substring(1, selectedGoals.toString().length() - 1));
				}
			});
		}

		btnAdd.setOnAction(e -> insertSession(dialog));
	}

	// Für jeden Belastungsgrad einen Radio-Button hinzufügen
	private void addRadioButtons(GridPane grid, int position, ToggleGroup groupLoad) {
		for (int i = 0; i < LoadDegree.values().length; i++) {
			RadioButton rbLoadDegree = new RadioButton(LoadDegree.valueOf(i).toString());
			rbLoadDegree.setUserData(LoadDegree.valueOf(i)); // Enum-Wert speichern
			rbLoadDegree.setToggleGroup(groupLoad);
			grid.add(rbLoadDegree, i + 1, position);
		}
	}

	// Liefert alle aktuell ausgewählten Ziele zurück
	private List<String> getSelectedItems() {
		List<String> goals = new ArrayList<>();
		for (MenuItem item : mbGoals.getItems()) {
			if (((CheckMenuItem) item).isSelected()) {
				goals.add(item.getText());
			}
		}
		return goals;
	}

	private void insertSession(Stage dialog) {
		List<String> goals = getSelectedItems();

		if (tfName.getText().isBlank() || tfTopic.getText().isBlank() || groupScope.getSelectedToggle() == null || groupIntensity.getSelectedToggle() == null
				|| groupPressure.getSelectedToggle() == null || groupAttention.getSelectedToggle() == null || groupTotal.getSelectedToggle() == null || goals.isEmpty()) {
			Alert alert = new Alert(AlertType.ERROR, "Es wurden noch nicht alle notwendigen Felder ausgefüllt.");
			alert.setHeaderText("Daten unvollständig");
			alert.showAndWait();
		} else {
			db.insertSession(new Session(tfName.getText(), tfTopic.getText(), (LoadDegree) groupScope.getSelectedToggle().getUserData(), (LoadDegree) groupIntensity.getSelectedToggle().getUserData(),
					(LoadDegree) groupPressure.getSelectedToggle().getUserData(), (LoadDegree) groupAttention.getSelectedToggle().getUserData(),
					(LoadDegree) groupTotal.getSelectedToggle().getUserData(), goals, plan));
			db.searchTableSessions(sessions, searchQuery);
			dialog.close();
		}
	}
}