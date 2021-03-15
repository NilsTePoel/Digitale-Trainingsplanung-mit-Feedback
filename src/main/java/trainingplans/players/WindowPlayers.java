package trainingplans.players;

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
import trainingplans.evaluations.WindowEvaluations;

public class WindowPlayers {
	private final Database db;
	private ObservableList<Player> players;
	private Player selectedPlayer;

	public WindowPlayers(Database db) {
		this.db = db;
	}

	public void open(Stage stage, double width, double height) {
		stage.setTitle("Spieler - Digitale Trainingsplanung mit Feedback");

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

		TableView<Player> table = new TableView<>();
		players = FXCollections.observableArrayList();
		table.setItems(players);
		TableColumn<Player, String> tableColName = new TableColumn<>("Name");
		tableColName.setCellValueFactory(new PropertyValueFactory<>("name"));
		table.getColumns().add(tableColName);
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
		ColumnConstraints columnOpenEvaluations = new ColumnConstraints();
		columnOpenEvaluations.setPercentWidth(40);
		ColumnConstraints columnAdd = new ColumnConstraints();
		columnAdd.setPercentWidth(10);
		ColumnConstraints columnBack = new ColumnConstraints();
		columnBack.setPercentWidth(10);
		gridActions.getColumnConstraints().addAll(columnName, columnEdit, columnDelete, columnOpenEvaluations, columnAdd, columnBack);
		gridActions.getRowConstraints().add(rowContent);

		Label lblName = new Label("Kein Spieler ausgewählt");
		lblName.setFont(Font.font("Tahoma", FontWeight.BOLD, 12));
		gridActions.add(lblName, 0, 0);

		Button btnEdit = new Button("Spieler bearbeiten");
		btnEdit.setDisable(true);
		gridActions.add(btnEdit, 1, 0);

		Button btnDelete = new Button("Spieler löschen");
		btnDelete.setDisable(true);
		gridActions.add(btnDelete, 2, 0);

		Button btnOpenEvaluations = new Button("Einschätzungen öffnen");
		btnOpenEvaluations.setDisable(true);
		gridActions.add(btnOpenEvaluations, 3, 0);

		Button btnAdd = new Button("Spieler hinzufügen");
		gridActions.add(btnAdd, 4, 0);

		Button btnBack = new Button("Zurück");
		gridActions.add(btnBack, 5, 0);

		grid.add(gridActions, 0, 2);

		Scene scene = new Scene(grid, width, height);
		stage.setScene(scene);
		stage.show();

		tfSearchQuery.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ENTER) {
				db.searchTablePlayers(players, tfSearchQuery.getText());
			}
		});

		btnSearch.setOnAction(e -> db.searchTablePlayers(players, tfSearchQuery.getText()));

		table.getSelectionModel().selectedItemProperty().addListener((ChangeListener<Player>) (observable, oldValue, newValue) -> {
			selectedPlayer = newValue;
			if (selectedPlayer != null) {
				lblName.setText(selectedPlayer.getName());
				btnEdit.setDisable(false);
				btnDelete.setDisable(false);
				btnOpenEvaluations.setDisable(false);
			} else {
				lblName.setText("Kein Spieler ausgewählt");
				btnEdit.setDisable(true);
				btnDelete.setDisable(true);
				btnOpenEvaluations.setDisable(true);
			}
		});

		// Einschätzungen des Spielers bei Doppelklick öffnen
		table.setOnMousePressed(e -> {
			if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
				WindowEvaluations windowEvaluations = new WindowEvaluations(db, selectedPlayer);
				windowEvaluations.open(stage, scene.getWidth(), scene.getHeight());
			}
		});

		btnEdit.setOnAction(e -> {
			WindowEditPlayer windowEditPlayer = new WindowEditPlayer(db, players, tfSearchQuery.getText(), selectedPlayer);
			windowEditPlayer.open(stage);
		});

		btnDelete.setOnAction(e -> {
			Alert alert = new Alert(AlertType.CONFIRMATION, "Soll der Spieler wirklich gelöscht werden?");
			alert.setHeaderText("Spieler löschen?");
			alert.showAndWait();
			if (alert.getResult() == ButtonType.OK) {
				db.removePlayer(selectedPlayer);
				db.searchTablePlayers(players, tfSearchQuery.getText());
			}
		});

		btnOpenEvaluations.setOnAction(e -> {
			WindowEvaluations windowEvaluations = new WindowEvaluations(db, selectedPlayer);
			windowEvaluations.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnAdd.setOnAction(e -> {
			WindowAddPlayer windowAddPlayer = new WindowAddPlayer(db, players, tfSearchQuery.getText());
			windowAddPlayer.open(stage);
		});

		btnBack.setOnAction(e -> {
			TrainingPlans trainingPlans = new TrainingPlans(db);
			trainingPlans.open(stage, scene.getWidth(), scene.getHeight());
		});

		db.searchTablePlayers(players, tfSearchQuery.getText());
	}
}
