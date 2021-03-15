package trainingplans.statistics;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import trainingplans.database.Database;
import trainingplans.database.LoadDegree;
import trainingplans.evaluations.Evaluation;
import trainingplans.players.Player;
import trainingplans.sessions.Session;

public class WindowEvaluationComparison extends WindowStatistics {
	private final ObservableList<Player> selectedPlayers;

	public WindowEvaluationComparison(Database db, ObservableList<Session> selectedData, ObservableList<Player> selectedPlayers) {
		super(db, selectedData);
		this.selectedPlayers = selectedPlayers;
	}

	@Override
	public void open(Stage stage, double width, double height) {
		stage.setTitle("Vergleich mit den Spielereinschätzungen - Digitale Trainingsplanung mit Feedback");

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));
		ColumnConstraints columnContent = new ColumnConstraints();
		columnContent.setPercentWidth(100);
		grid.getColumnConstraints().add(columnContent);
		RowConstraints rowLineChart = new RowConstraints();
		rowLineChart.setPercentHeight(90);
		RowConstraints rowActions = new RowConstraints();
		rowActions.setPercentHeight(10);
		grid.getRowConstraints().addAll(rowLineChart, rowActions);

		CategoryAxis xAxis = new CategoryAxis();
		CategoryAxis yAxis = new CategoryAxis();
		xAxis.setLabel("Trainingseinheit");
		yAxis.setLabel("Belastungsgrad");

		// Belastungsgrade sortieren
		Set<String> categories = new LinkedHashSet<>();
		categories.add("Fehlt"); // Wird verwendet, falls eine Spielereinschätzung fehlt
		for (LoadDegree value : LoadDegree.values()) {
			categories.add(value.toString());
		}

		ObservableList<String> c = FXCollections.observableArrayList(categories);
		Collections.sort(c);
		yAxis.setCategories(c);

		LineChart<String, String> lineChart = new LineChart<>(xAxis, yAxis);
		lineChart.setTitle("Vergleich mit den Spielereinschätzungen");
		lineChart.setAnimated(false);

		XYChart.Series<String, String> seriesTrainer = new XYChart.Series<>();
		seriesTrainer.setName("Trainer");

		// Trainereinschätzungen
		for (Session session : selectedData) {
			seriesTrainer.getData().add(new XYChart.Data<>(session.getName(), session.getScope().toString()));
		}

		lineChart.getData().add(seriesTrainer);

		// Zugehörige Spielereinschätzungen (pro Spieler)
		for (Player player : selectedPlayers) {
			XYChart.Series<String, String> seriesPlayer = new XYChart.Series<>();
			seriesPlayer.setName(player.getName());

			for (Session session : selectedData) {
				ObservableList<Evaluation> evaluations = FXCollections.observableArrayList();
				db.searchTableEvaluations(evaluations, player, session.getName());
				if (!evaluations.isEmpty()) {
					Evaluation evaluation = evaluations.get(0); // Jedes Datum sollte eigentlich nur einmal vorkommen
					seriesPlayer.getData().add(new XYChart.Data<>(evaluation.getName(), evaluation.getLoad().toString()));
				} else {
					seriesPlayer.getData().add(new XYChart.Data<>(session.getName(), "Fehlt"));
				}
			}

			lineChart.getData().add(seriesPlayer);
		}

		grid.add(lineChart, 0, 0);

		GridPane gridActions = new GridPane();
		ColumnConstraints columnActions = new ColumnConstraints();
		columnActions.setPercentWidth(90);
		ColumnConstraints columnBack = new ColumnConstraints();
		columnBack.setPercentWidth(10);
		gridActions.getColumnConstraints().addAll(columnActions, columnBack);
		RowConstraints rowContent = new RowConstraints();
		rowContent.setPercentHeight(100);
		gridActions.getRowConstraints().add(rowContent);

		Button btnBack = new Button("Zurück");
		gridActions.add(btnBack, 1, 0);

		grid.add(gridActions, 0, 1);

		Scene scene = new Scene(grid, width, height);
		stage.setScene(scene);
		stage.show();

		btnBack.setOnAction(e -> {
			WindowSelectPlayers windowSelectPlayers = new WindowSelectPlayers(db, selectedData);
			windowSelectPlayers.open(stage, scene.getWidth(), scene.getHeight());
		});
	}
}