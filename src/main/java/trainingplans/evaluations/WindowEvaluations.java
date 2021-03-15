package trainingplans.evaluations;

import java.util.Arrays;

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
import trainingplans.database.Database;
import trainingplans.players.Player;
import trainingplans.players.WindowPlayers;

public class WindowEvaluations {
	private final Database db;
	private ObservableList<Evaluation> evaluations;
	private final Player player;
	private Evaluation selectedEvaluation;

	public WindowEvaluations(Database db, Player player) {
		this.db = db;
		this.player = player;
	}

	public void open(Stage stage, double width, double height) {
		stage.setTitle("Spielereinschätzungen (" + player.getName() + ") - Digitale Trainingsplanung mit Feedback");

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

		TableView<Evaluation> table = new TableView<>();
		evaluations = FXCollections.observableArrayList();
		table.setItems(evaluations);
		TableColumn<Evaluation, String> tableColName = new TableColumn<>("Datum");
		tableColName.setCellValueFactory(new PropertyValueFactory<>("name"));
		TableColumn<Evaluation, String> tableColLoad = new TableColumn<>("Gesamtbelastung/-beanspruchung");
		tableColLoad.setCellValueFactory(new PropertyValueFactory<>("load"));
		TableColumn<Evaluation, String> tableColGoals = new TableColumn<>("Trainingsziele");
		tableColGoals.setCellValueFactory(new PropertyValueFactory<>("goals"));
		table.getColumns().addAll(Arrays.asList(tableColName, tableColLoad, tableColGoals));
		table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		gridTable.add(table, 0, 0);

		grid.add(gridTable, 0, 1);

		GridPane gridActions = new GridPane();
		ColumnConstraints columnName = new ColumnConstraints();
		columnName.setPercentWidth(20);
		ColumnConstraints columnEdit = new ColumnConstraints();
		columnEdit.setPercentWidth(10);
		ColumnConstraints columnDelete = new ColumnConstraints();
		columnDelete.setPercentWidth(50);
		ColumnConstraints columnAdd = new ColumnConstraints();
		columnAdd.setPercentWidth(10);
		ColumnConstraints columnBack = new ColumnConstraints();
		columnBack.setPercentWidth(10);
		gridActions.getColumnConstraints().addAll(columnName, columnEdit, columnDelete, columnAdd, columnBack);
		gridActions.getRowConstraints().add(rowContent);

		Label lblName = new Label("Keine Spielereinschätzung ausgewählt");
		lblName.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
		gridActions.add(lblName, 0, 0);

		Button btnEdit = new Button("Einschätzung bearbeiten");
		btnEdit.setDisable(true);
		gridActions.add(btnEdit, 1, 0);

		Button btnDelete = new Button("Einschätzung löschen");
		btnDelete.setDisable(true);
		gridActions.add(btnDelete, 2, 0);

		Button btnAdd = new Button("Einschätzung hinzufügen");
		gridActions.add(btnAdd, 3, 0);

		Button btnBack = new Button("Zurück");
		gridActions.add(btnBack, 4, 0);

		grid.add(gridActions, 0, 2);

		Scene scene = new Scene(grid, width, height);
		stage.setScene(scene);
		stage.show();

		tfSearchQuery.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				db.searchTableEvaluations(evaluations, player, tfSearchQuery.getText());
			}
		});

		btnSearch.setOnAction(e -> db.searchTableEvaluations(evaluations, player, tfSearchQuery.getText()));

		table.getSelectionModel().selectedItemProperty().addListener((ChangeListener<Evaluation>) (observable, oldValue, newValue) -> {
			selectedEvaluation = newValue;
			if (selectedEvaluation != null) {
				lblName.setText(selectedEvaluation.getName());
				btnEdit.setDisable(false);
				btnDelete.setDisable(false);
			} else {
				lblName.setText("Keine Spielereinschätzung ausgewählt");
				btnEdit.setDisable(true);
				btnDelete.setDisable(true);
			}
		});

		btnEdit.setOnAction(e -> {
			WindowEditEvaluation windowEditEvaluation = new WindowEditEvaluation(db, evaluations, player, tfSearchQuery.getText(), selectedEvaluation);
			windowEditEvaluation.open(stage);
		});

		btnDelete.setOnAction(e -> {
			Alert alert = new Alert(AlertType.CONFIRMATION, "Soll die Spielereinschätzung wirklich gelöscht werden?");
			alert.setHeaderText("Einschätzung löschen?");
			alert.showAndWait();
			if (alert.getResult() == ButtonType.OK) {
				db.removeEvaluation(selectedEvaluation);
				db.searchTableEvaluations(evaluations, player, tfSearchQuery.getText());
			}
		});

		btnAdd.setOnAction(e -> {
			WindowAddEvaluation windowAddEvaluation = new WindowAddEvaluation(db, evaluations, player, tfSearchQuery.getText());
			windowAddEvaluation.open(stage);
		});

		btnBack.setOnAction(e -> {
			WindowPlayers windowPlayers = new WindowPlayers(db);
			windowPlayers.open(stage, scene.getWidth(), scene.getHeight());
		});

		db.searchTableEvaluations(evaluations, player, tfSearchQuery.getText());
	}
}
