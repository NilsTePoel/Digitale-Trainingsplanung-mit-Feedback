package trainingplans.statistics;

import java.util.Arrays;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
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
import trainingplans.sessions.Session;

public class WindowSelectSessions {
	private final Database db;
	private ObservableList<Session> sessions;
	private ObservableList<Session> selectedSessions;

	public WindowSelectSessions(Database db) {
		this.db = db;
	}

	public void open(Stage stage, double width, double height) {
		stage.setTitle("Trainingseinheiten auswählen - Digitale Trainingsplanung mit Feedback");

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
		TableColumn<Session, Integer> tableColScope = new TableColumn<>("Umfang");
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
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); // Auswahl von mehreren Einheiten zulassen
		gridTable.add(table, 0, 0);

		grid.add(gridTable, 0, 1);

		GridPane gridActions = new GridPane();
		ColumnConstraints columnCount = new ColumnConstraints();
		columnCount.setPercentWidth(20);
		ColumnConstraints columnConfirmSelection = new ColumnConstraints();
		columnConfirmSelection.setPercentWidth(60);
		ColumnConstraints columnSelectAll = new ColumnConstraints();
		columnSelectAll.setPercentWidth(10);
		ColumnConstraints columnBack = new ColumnConstraints();
		columnBack.setPercentWidth(10);
		gridActions.getColumnConstraints().addAll(columnCount, columnConfirmSelection, columnSelectAll, columnBack);
		gridActions.getRowConstraints().add(rowContent);

		Label lblCount = new Label("0 Trainingseinheiten ausgewählt");
		lblCount.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
		gridActions.add(lblCount, 0, 0);

		Button btnConfirmSelection = new Button("Auswahl bestätigen");
		btnConfirmSelection.setDisable(true);
		gridActions.add(btnConfirmSelection, 1, 0);

		Button btnSelectAll = new Button("Alles auswählen");
		gridActions.add(btnSelectAll, 2, 0);

		Button btnBack = new Button("Zurück");
		gridActions.add(btnBack, 3, 0);

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

		// Label an die aktuelle Auswahl anpassen
		selectedSessions = table.getSelectionModel().getSelectedItems();
		selectedSessions.addListener((ListChangeListener<Session>) change -> {
			lblCount.setText(selectedSessions.size() + " Trainingseinheiten ausgewählt");

			// Auswahl-Button deaktivieren, wenn die Auswahl leer ist
			if (selectedSessions.isEmpty()) {
				btnConfirmSelection.setDisable(true);
			} else {
				btnConfirmSelection.setDisable(false);
			}
		});

		btnConfirmSelection.setOnAction(e -> {
			WindowStatistics windowStatistics = new WindowStatistics(db, selectedSessions);
			windowStatistics.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnSelectAll.setOnAction(e -> {
			WindowStatistics windowStatistics = new WindowStatistics(db, sessions);
			windowStatistics.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnBack.setOnAction(e -> {
			TrainingPlans trainingPlans = new TrainingPlans(db);
			trainingPlans.open(stage, scene.getWidth(), scene.getHeight());
		});

		db.searchTableSessions(sessions, tfSearchQuery.getText());
	}
}