package trainingplans.goals;

import java.util.Arrays;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import trainingplans.TrainingPlans;
import trainingplans.database.Database;

public class WindowGoals {
	private final Database db;
	private ObservableList<Goal> goals;
	private Goal selectedGoal;

	public WindowGoals(Database db) {
		this.db = db;
	}

	public void open(Stage stage, double width, double height) {
		stage.setTitle("Trainingsziele - Digitale Trainingsplanung mit Feedback");

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));
		ColumnConstraints columnContent = new ColumnConstraints();
		columnContent.setPercentWidth(100);
		grid.getColumnConstraints().add(columnContent);
		RowConstraints rowSearch = new RowConstraints();
		rowSearch.setPercentHeight(10);
		RowConstraints rowTable = new RowConstraints();
		rowTable.setPercentHeight(80);
		RowConstraints rowActions = new RowConstraints();
		rowActions.setPercentHeight(10);
		grid.getRowConstraints().addAll(rowSearch, rowTable, rowActions);

		GridPane gridSearch = new GridPane();
		gridSearch.setHgap(10);
		ColumnConstraints columnSearchQuery = new ColumnConstraints();
		columnSearchQuery.setPercentWidth(90);
		ColumnConstraints columnSearch = new ColumnConstraints();
		columnSearch.setPercentWidth(10);
		gridSearch.getColumnConstraints().addAll(columnSearchQuery, columnSearch);
		RowConstraints rowContent = new RowConstraints();
		rowContent.setPercentHeight(100);
		gridSearch.getRowConstraints().add(rowContent);

		TextField tfSearchQuery = new TextField();
		gridSearch.add(tfSearchQuery, 0, 0);

		Button btnSearch = new Button("Suchen");
		btnSearch.setMaxWidth(Double.MAX_VALUE);
		gridSearch.add(btnSearch, 1, 0);

		grid.add(gridSearch, 0, 0);

		GridPane gridTable = new GridPane();
		gridTable.getColumnConstraints().add(columnContent);
		gridTable.getRowConstraints().add(rowContent);

		TableView<Goal> table = new TableView<>();
		goals = FXCollections.observableArrayList();
		table.setItems(goals);
		TableColumn<Goal, String> tableColAbbreviation = new TableColumn<>("Abkürzung");
		tableColAbbreviation.setCellValueFactory(new PropertyValueFactory<>("abbreviation"));
		TableColumn<Goal, String> tableColName = new TableColumn<>("Name");
		tableColName.setCellValueFactory(new PropertyValueFactory<>("name"));
		table.getColumns().addAll(Arrays.asList(tableColAbbreviation, tableColName));
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		gridTable.add(table, 0, 0);

		grid.add(gridTable, 0, 1);

		GridPane gridActions = new GridPane();
		ColumnConstraints columnName = new ColumnConstraints();
		columnName.setPercentWidth(20);
		ColumnConstraints columnEdit = new ColumnConstraints();
		columnEdit.setPercentWidth(10);
		ColumnConstraints columnDelete = new ColumnConstraints();
		columnDelete.setPercentWidth(35);
		ColumnConstraints columnDefaultGoals = new ColumnConstraints();
		columnDefaultGoals.setPercentWidth(15);
		ColumnConstraints columnAdd = new ColumnConstraints();
		columnAdd.setPercentWidth(10);
		ColumnConstraints columnBack = new ColumnConstraints();
		columnBack.setPercentWidth(10);
		gridActions.getColumnConstraints().addAll(columnName, columnEdit, columnDelete, columnDefaultGoals, columnAdd, columnBack);
		gridActions.getRowConstraints().add(rowContent);

		Label lblName = new Label("Kein Trainingsziel ausgewählt");
		lblName.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
		gridActions.add(lblName, 0, 0);

		Button btnEdit = new Button("Ziel bearbeiten");
		btnEdit.setDisable(true);
		gridActions.add(btnEdit, 1, 0);

		Button btnDelete = new Button("Ziel löschen");
		btnDelete.setDisable(true);
		gridActions.add(btnDelete, 2, 0);

		Button btnDefaultGoals = new Button("Standardziele hinzufügen");
		gridActions.add(btnDefaultGoals, 3, 0);

		Button btnAdd = new Button("Ziel hinzufügen");
		gridActions.add(btnAdd, 4, 0);

		Button btnBack = new Button("Zurück");
		gridActions.add(btnBack, 5, 0);

		grid.add(gridActions, 0, 2);

		Scene scene = new Scene(grid, width, height);
		stage.setScene(scene);
		stage.show();

		tfSearchQuery.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				db.searchTableGoals(goals, tfSearchQuery.getText());
			}
		});

		btnSearch.setOnAction(e -> db.searchTableGoals(goals, tfSearchQuery.getText()));

		table.getSelectionModel().selectedItemProperty().addListener((ChangeListener<Goal>) (observable, oldValue, newValue) -> {
			selectedGoal = newValue;
			if (selectedGoal != null) {
				lblName.setText(selectedGoal.getName());
				btnEdit.setDisable(false);
				btnDelete.setDisable(false);
			} else {
				lblName.setText("Kein Trainingsziel ausgewählt");
				btnEdit.setDisable(true);
				btnDelete.setDisable(true);
			}
		});

		goals.addListener((ListChangeListener<Goal>) change -> {
			if (goals.isEmpty()) {
				btnDefaultGoals.setText("Standardziele hinzufügen");
			} else {
				btnDefaultGoals.setText("Standardziele wiederherstellen");
			}
		});

		btnEdit.setOnAction(e -> {
			WindowEditGoal windowEditGoal = new WindowEditGoal(db, goals, tfSearchQuery.getText(), selectedGoal);
			windowEditGoal.open(stage);
		});

		btnDelete.setOnAction(e -> {
			Alert alert = new Alert(AlertType.CONFIRMATION, "Soll das Trainingsziel wirklich gelöscht werden?");
			alert.setHeaderText("Ziel löschen?");
			alert.showAndWait();
			if (alert.getResult() == ButtonType.OK) {
				db.removeGoal(selectedGoal);
				db.searchTableGoals(goals, tfSearchQuery.getText());
			}
		});

		btnDefaultGoals.setOnAction(e -> {
			if (!goals.isEmpty()) {
				Alert alert = new Alert(AlertType.CONFIRMATION, "Sollen die Standardziele wiederhergestellt werden?\nAlle aktuellen Ziele werden gelöscht.");
				alert.setHeaderText("Standardziele wiederherstellen?");
				alert.showAndWait();
				if (alert.getResult() == ButtonType.OK) {
					db.insertDefaultGoals();
					db.searchTableGoals(goals, tfSearchQuery.getText());
				}
			} else {
				db.insertDefaultGoals();
				db.searchTableGoals(goals, tfSearchQuery.getText());
			}
		});

		btnAdd.setOnAction(e -> {
			WindowAddGoal windowAddGoal = new WindowAddGoal(db, goals, tfSearchQuery.getText());
			windowAddGoal.open(stage);
		});

		btnBack.setOnAction(e -> {
			TrainingPlans trainingPlans = new TrainingPlans(db);
			trainingPlans.open(stage, scene.getWidth(), scene.getHeight());
		});

		db.searchTableGoals(goals, tfSearchQuery.getText());
	}
}
