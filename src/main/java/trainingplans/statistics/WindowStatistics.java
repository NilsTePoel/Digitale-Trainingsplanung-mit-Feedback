package trainingplans.statistics;

import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import trainingplans.database.Database;
import trainingplans.sessions.Session;

public class WindowStatistics {
	protected Database db;
	protected ObservableList<Session> selectedData;

	public WindowStatistics(Database db, ObservableList<Session> selectedData) {
		this.db = db;
		this.selectedData = selectedData;
	}

	public void open(Stage stage, double width, double height) {
		stage.setTitle("Statistiken - Digitale Trainingsplanung mit Feedback");

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(10, 10, 10, 10));
		ColumnConstraints columnContent = new ColumnConstraints();
		columnContent.setPercentWidth(100);
		grid.getColumnConstraints().add(columnContent);
		RowConstraints rowStatistics = new RowConstraints();
		rowStatistics.setPercentHeight(90);
		RowConstraints rowBack = new RowConstraints();
		rowBack.setPercentHeight(10);
		grid.getRowConstraints().addAll(rowStatistics, rowBack);

		GridPane gridStatistics = new GridPane();
		gridStatistics.setAlignment(Pos.CENTER);
		gridStatistics.setVgap(10);

		GridPane gridTitle = new GridPane();
		gridTitle.setAlignment(Pos.CENTER);
		Text title = new Text("Statistiken");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 20));
		gridTitle.add(title, 0, 0);
		gridStatistics.add(gridTitle, 0, 0);

		Button btnLoadHistory = new Button("Belastungsverlauf aus Trainersicht");
		btnLoadHistory.setMaxWidth(Double.MAX_VALUE);
		gridStatistics.add(btnLoadHistory, 0, 1);

		Button btnGoalHistory = new Button("Trainingszielverlauf aus Trainersicht");
		btnGoalHistory.setMaxWidth(Double.MAX_VALUE);
		gridStatistics.add(btnGoalHistory, 0, 2);

		Button btnEvaluationComparison = new Button("Vergleich mit den Spielereinschätzungen");
		btnEvaluationComparison.setMaxWidth(Double.MAX_VALUE);
		gridStatistics.add(btnEvaluationComparison, 0, 3);

		grid.add(gridStatistics, 0, 0);

		GridPane gridBack = new GridPane();
		ColumnConstraints columnActions = new ColumnConstraints();
		columnActions.setPercentWidth(90);
		ColumnConstraints columnBack = new ColumnConstraints();
		columnBack.setPercentWidth(10);
		gridBack.getColumnConstraints().addAll(columnActions, columnBack);
		RowConstraints rowContent = new RowConstraints();
		rowContent.setPercentHeight(100);
		gridBack.getRowConstraints().add(rowContent);

		Button btnBack = new Button("Zurück");
		gridBack.add(btnBack, 1, 0);
		grid.add(gridBack, 0, 1);

		Scene scene = new Scene(grid, width, height);
		stage.setScene(scene);
		stage.show();

		btnLoadHistory.setOnAction(e -> {
			WindowLoadHistory windowLoadHistory = new WindowLoadHistory(db, selectedData);
			windowLoadHistory.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnGoalHistory.setOnAction(e -> {
			WindowGoalHistory windowGoalHistory = new WindowGoalHistory(db, selectedData);
			windowGoalHistory.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnEvaluationComparison.setOnAction(e -> {
			WindowSelectPlayers windowSelectPlayers = new WindowSelectPlayers(db, selectedData);
			windowSelectPlayers.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnBack.setOnAction(e -> {
			WindowSelectSessions windowSelectSessions = new WindowSelectSessions(db);
			windowSelectSessions.open(stage, scene.getWidth(), scene.getHeight());
		});
	}
}