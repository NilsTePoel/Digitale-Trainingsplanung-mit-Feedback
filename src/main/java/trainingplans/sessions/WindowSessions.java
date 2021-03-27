package trainingplans.sessions;

import java.util.ArrayList;
import java.util.Arrays;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
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
import trainingplans.utils.FileAccess;

public class WindowSessions {
	private final Database db;
	private ObservableList<Session> sessions;
	private Session selectedSession;

	public WindowSessions(Database db) {
		this.db = db;
	}

	public void open(Stage stage, double width, double height) {
		stage.setTitle("Trainingseinheiten und Trainereinschätzungen - Digitale Trainingsplanung mit Feedback");

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

		TableView<Session> table = new TableView<>();
		sessions = FXCollections.observableArrayList();
		table.setItems(sessions);
		TableColumn<Session, String> tableColName = new TableColumn<>("Datum");
		tableColName.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumn<Session, String> tableColScope = new TableColumn<>("Umfang");
		tableColScope.setCellValueFactory(new PropertyValueFactory<>("scope"));
		TableColumn<Session, Integer> tableColIntensity = new TableColumn<>("Intensität");
		tableColIntensity.setCellValueFactory(new PropertyValueFactory<>("intensity"));
		TableColumn<Session, Integer> tableColPressure = new TableColumn<>("Druckbedingungen");
		tableColPressure.setCellValueFactory(new PropertyValueFactory<>("pressure"));
		TableColumn<Session, Integer> tableColAttention = new TableColumn<>("Aufmerksamkeit/Konzentration");
		tableColAttention.setCellValueFactory(new PropertyValueFactory<>("attention"));
		TableColumn<Session, Integer> tableColTotal = new TableColumn<>("Gesamt");
		tableColTotal.setCellValueFactory(new PropertyValueFactory<>("total"));
		TableColumn<Session, String> tableColGoals = new TableColumn<>("Trainingsziele");
		tableColGoals.setCellValueFactory(p -> new SimpleStringProperty(p.getValue().getGoals().toString().substring(1, p.getValue().getGoals().toString().length() - 1)));
		table.getColumns().addAll(Arrays.asList(tableColName, tableColScope, tableColIntensity, tableColPressure, tableColAttention, tableColTotal, tableColGoals));
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		gridTable.add(table, 0, 0);

		grid.add(gridTable, 0, 1);

		GridPane gridActions = new GridPane();
		ColumnConstraints columnName = new ColumnConstraints();
		columnName.setPercentWidth(20);
		ColumnConstraints columnEdit = new ColumnConstraints();
		columnEdit.setPercentWidth(10);
		ColumnConstraints columnDelete = new ColumnConstraints();
		columnDelete.setPercentWidth(10);
		ColumnConstraints columnOpenPlan = new ColumnConstraints();
		columnOpenPlan.setPercentWidth(40);
		ColumnConstraints columnAdd = new ColumnConstraints();
		columnAdd.setPercentWidth(10);
		ColumnConstraints columnBack = new ColumnConstraints();
		columnBack.setPercentWidth(10);
		gridActions.getColumnConstraints().addAll(columnName, columnEdit, columnDelete, columnOpenPlan, columnAdd, columnBack);
		gridActions.getRowConstraints().add(rowContent);

		Label lblName = new Label("Keine Trainingseinheit ausgewählt");
		lblName.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
		gridActions.add(lblName, 0, 0);

		Button btnEdit = new Button("Einheit bearbeiten");
		btnEdit.setDisable(true);
		gridActions.add(btnEdit, 1, 0);

		Button btnDelete = new Button("Einheit löschen");
		btnDelete.setDisable(true);
		gridActions.add(btnDelete, 2, 0);

		Button btnOpenPlan = new Button("Angehängten Plan öffnen");
		btnOpenPlan.setDisable(true);
		gridActions.add(btnOpenPlan, 3, 0);

		Button btnAdd = new Button("Einheit hinzufügen");
		gridActions.add(btnAdd, 4, 0);

		Button btnBack = new Button("Zurück");
		gridActions.add(btnBack, 5, 0);

		grid.add(gridActions, 0, 2);

		Scene scene = new Scene(grid, width, height);
		stage.setScene(scene);
		stage.show();

		tfSearchQuery.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				db.searchTableSessions(sessions, tfSearchQuery.getText());
			}
		});

		btnSearch.setOnAction(e -> db.searchTableSessions(sessions, tfSearchQuery.getText()));

		table.getSelectionModel().selectedItemProperty().addListener((ChangeListener<Session>) (observable, oldValue, newValue) -> {
			selectedSession = newValue;
			if (selectedSession != null) {
				lblName.setText(selectedSession.getName());
				btnEdit.setDisable(false);
				btnDelete.setDisable(false);
				btnOpenPlan.setDisable(selectedSession.getPlan().equals("null"));
			} else {
				lblName.setText("Keine Trainingseinheit ausgewählt");
				btnEdit.setDisable(true);
				btnDelete.setDisable(true);
				btnOpenPlan.setDisable(true);
			}
		});

		// Trainingsplan bei Doppelklick öffnen
		table.setOnMousePressed(e -> {
			if (selectedSession != null && !selectedSession.getPlan().equals("null") && e.isPrimaryButtonDown() && e.getClickCount() == 2) {
				FileAccess trainingPlan = new FileAccess(selectedSession.getPlan(), "Der angefügte Trainingsplan");
				trainingPlan.open();
			}
		});

		btnEdit.setOnAction(e -> {
			WindowEditSession windowEditSession = new WindowEditSession(db, sessions, tfSearchQuery.getText(), selectedSession);
			windowEditSession.open(stage);
		});

		btnDelete.setOnAction(e -> {
			Alert alert = new Alert(AlertType.CONFIRMATION, "Soll die Trainingseinheit wirklich gelöscht werden?");
			alert.setHeaderText("Einheit löschen?");
			alert.showAndWait();
			if (alert.getResult() == ButtonType.OK) {
				db.removeSession(selectedSession);
				db.searchTableSessions(sessions, tfSearchQuery.getText());
			}
		});

		btnOpenPlan.setOnAction(e -> {
			FileAccess trainingPlan = new FileAccess(selectedSession.getPlan(), "Der angefügte Trainingsplan");
			trainingPlan.open();
		});

		btnAdd.setOnAction(e -> {
			ArrayList<String> goals = db.getGoals();
			if (goals.isEmpty()) {
				Alert alert = new Alert(AlertType.ERROR,
						"Die Liste der möglichen Trainingsziele ist leer.\nBitte legen Sie erst unter dem Menüpunkt \"Trainingsziele\" auf der Startseite die Liste der möglichen Trainingsziele fest.");
				alert.setHeaderText("Keine Trainingsziele");
				alert.showAndWait();
			} else {
				WindowAddSession windowAddSession = new WindowAddSession(db, sessions, tfSearchQuery.getText(), goals);
				windowAddSession.open(stage);
			}
		});

		btnBack.setOnAction(e -> {
			TrainingPlans trainingPlans = new TrainingPlans(db);
			trainingPlans.open(stage, scene.getWidth(), scene.getHeight());
		});

		db.searchTableSessions(sessions, tfSearchQuery.getText());
	}
}