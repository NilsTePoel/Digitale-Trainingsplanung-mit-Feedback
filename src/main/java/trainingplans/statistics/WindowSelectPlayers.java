package trainingplans.statistics;

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
import trainingplans.database.Database;
import trainingplans.players.Player;
import trainingplans.sessions.Session;

public class WindowSelectPlayers extends WindowStatistics {
	private ObservableList<Player> players;
	private ObservableList<Player> selectedPlayers;

	public WindowSelectPlayers(Database db, ObservableList<Session> selectedData) {
		super(db, selectedData);
	}

	@Override
	public void open(Stage stage, double width, double height) {
		stage.setTitle("Spieler auswählen - Digitale Trainingsplanung mit Feedback");

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
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE); // Auswahl von mehreren Spielern zulassen
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

		Label lblCount = new Label("0 Spieler ausgewählt");
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
				db.searchTablePlayers(players, tfSearchQuery.getText());
			}
		});

		btnSearch.setOnAction(e -> db.searchTablePlayers(players, tfSearchQuery.getText()));

		// Label an die aktuelle Auswahl anpassen
		selectedPlayers = table.getSelectionModel().getSelectedItems();
		selectedPlayers.addListener((ListChangeListener<Player>) change -> {
			lblCount.setText(selectedPlayers.size() + " Spieler ausgewählt");

			// Auswahl-Button deaktivieren, wenn die Auswahl leer ist
			if (selectedPlayers.isEmpty()) {
				btnConfirmSelection.setDisable(true);
			} else {
				btnConfirmSelection.setDisable(false);
			}
		});

		btnConfirmSelection.setOnAction(e -> {
			WindowEvaluationComparison windowEvaluationComparison = new WindowEvaluationComparison(db, selectedData, selectedPlayers);
			windowEvaluationComparison.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnSelectAll.setOnAction(e -> {
			WindowEvaluationComparison windowEvaluationComparison = new WindowEvaluationComparison(db, selectedData, players);
			windowEvaluationComparison.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnBack.setOnAction(e -> {
			WindowStatistics windowStatistics = new WindowStatistics(db, selectedData);
			windowStatistics.open(stage, scene.getWidth(), scene.getHeight());
		});

		db.searchTablePlayers(players, tfSearchQuery.getText());
	}
}
