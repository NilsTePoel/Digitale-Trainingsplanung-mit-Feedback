package trainingplans.statistics;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import trainingplans.database.Database;
import trainingplans.sessions.Session;

public class WindowGoalHistory extends WindowStatistics {
	public WindowGoalHistory(Database db, ObservableList<Session> selectedData) {
		super(db, selectedData);
	}

	@Override
	public void open(Stage stage, double width, double height) {
		stage.setTitle("Trainingszielverlauf aus Trainersicht - Digitale Trainingsplanung mit Feedback");

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
		NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("Trainingsziel");
		yAxis.setLabel("Häufigkeit");

		BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
		barChart.setTitle("Trainingszielverlauf aus Trainersicht");
		barChart.setAnimated(false);
		barChart.setLegendVisible(false);

		XYChart.Series<String, Number> series = new XYChart.Series<>();
		for (String goal : db.getGoals()) {
			series.getData().add(new XYChart.Data<>(goal, db.getGoalCount(selectedData, goal)));
		}
		barChart.getData().add(series);

		grid.add(barChart, 0, 0);

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
			WindowStatistics windowStatistics = new WindowStatistics(db, selectedData);
			windowStatistics.open(stage, scene.getWidth(), scene.getHeight());
		});
	}
}