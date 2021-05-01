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
import javafx.scene.control.CheckBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;
import trainingplans.database.Database;
import trainingplans.database.LoadDegree;
import trainingplans.sessions.Session;

public class WindowLoadHistory extends WindowStatistics {
	public WindowLoadHistory(Database db, ObservableList<Session> selectedData) {
		super(db, selectedData);
	}

	@Override
	public void open(Stage stage, double width, double height) {
		stage.setTitle("Belastungsverlauf aus Trainersicht - Digitale Trainingsplanung mit Feedback");

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
		for (LoadDegree value : LoadDegree.values()) {
			categories.add(value.toString());
		}

		ObservableList<String> c = FXCollections.observableArrayList(categories);
		Collections.sort(c);
		yAxis.setCategories(c);

		LineChart<String, String> lineChart = new LineChart<>(xAxis, yAxis);
		lineChart.setTitle("Belastungsverlauf aus Trainersicht");
		lineChart.setAnimated(false);

		XYChart.Series<String, String> seriesScope = new XYChart.Series<>();
		seriesScope.setName("Umfang");

		XYChart.Series<String, String> seriesIntensity = new XYChart.Series<>();
		seriesIntensity.setName("Intensität");

		XYChart.Series<String, String> seriesPressure = new XYChart.Series<>();
		seriesPressure.setName("Druckbedingungen");

		XYChart.Series<String, String> seriesAttention = new XYChart.Series<>();
		seriesAttention.setName("Aufmerksamkeit/Konzentration");

		XYChart.Series<String, String> seriesTotal = new XYChart.Series<>();
		seriesTotal.setName("Gesamt");

		for (Session session : selectedData) {
			String label = session.getName() + " (" + session.getTopic() + ")";
			seriesScope.getData().add(new XYChart.Data<>(label, session.getScope().toString()));
			seriesIntensity.getData().add(new XYChart.Data<>(label, session.getIntensity().toString()));
			seriesPressure.getData().add(new XYChart.Data<>(label, session.getPressure().toString()));
			seriesAttention.getData().add(new XYChart.Data<>(label, session.getAttention().toString()));
			seriesTotal.getData().add(new XYChart.Data<>(label, session.getTotal().toString()));
		}

		lineChart.getData().add(seriesScope);
		lineChart.getData().add(seriesIntensity);
		lineChart.getData().add(seriesPressure);
		lineChart.getData().add(seriesAttention);
		lineChart.getData().add(seriesTotal);

		grid.add(lineChart, 0, 0);

		GridPane gridActions = new GridPane();
		ColumnConstraints columnScope = new ColumnConstraints();
		columnScope.setPercentWidth(15);
		ColumnConstraints columnIntensity = new ColumnConstraints();
		columnIntensity.setPercentWidth(15);
		ColumnConstraints columnPressure = new ColumnConstraints();
		columnPressure.setPercentWidth(15);
		ColumnConstraints columnAttention = new ColumnConstraints();
		columnAttention.setPercentWidth(15);
		ColumnConstraints columnTotal = new ColumnConstraints();
		columnTotal.setPercentWidth(30);
		ColumnConstraints columnBack = new ColumnConstraints();
		columnBack.setPercentWidth(10);
		gridActions.getColumnConstraints().addAll(columnScope, columnIntensity, columnPressure, columnAttention, columnTotal, columnBack);
		RowConstraints rowContent = new RowConstraints();
		rowContent.setPercentHeight(100);
		gridActions.getRowConstraints().add(rowContent);

		CheckBox cbContent = new CheckBox("Umfang");
		cbContent.setSelected(true);
		gridActions.add(cbContent, 0, 0);

		CheckBox cbIntensity = new CheckBox("Intensität");
		cbIntensity.setSelected(true);
		gridActions.add(cbIntensity, 1, 0);

		CheckBox cbPressure = new CheckBox("Druckbedingungen");
		cbPressure.setSelected(true);
		gridActions.add(cbPressure, 2, 0);

		CheckBox cbAttention = new CheckBox("Aufmerksamkeit/Konzentration");
		cbAttention.setSelected(true);
		gridActions.add(cbAttention, 3, 0);

		CheckBox cbTotal = new CheckBox("Gesamt");
		cbTotal.setSelected(true);
		gridActions.add(cbTotal, 4, 0);

		Button btnBack = new Button("Zurück");
		gridActions.add(btnBack, 5, 0);

		grid.add(gridActions, 0, 1);

		Scene scene = new Scene(grid, width, height);
		stage.setScene(scene);
		stage.show();

		cbContent.setOnAction(e -> {
			if (cbContent.isSelected()) {
				lineChart.getData().add(seriesScope);
			} else {
				lineChart.getData().remove(seriesScope);
			}
		});

		cbIntensity.setOnAction(e -> {
			if (cbIntensity.isSelected()) {
				lineChart.getData().add(seriesIntensity);
			} else {
				lineChart.getData().remove(seriesIntensity);
			}
		});

		cbPressure.setOnAction(e -> {
			if (cbPressure.isSelected()) {
				lineChart.getData().add(seriesPressure);
			} else {
				lineChart.getData().remove(seriesPressure);
			}
		});

		cbAttention.setOnAction(e -> {
			if (cbAttention.isSelected()) {
				lineChart.getData().add(seriesAttention);
			} else {
				lineChart.getData().remove(seriesAttention);
			}
		});

		cbTotal.setOnAction(e -> {
			if (cbTotal.isSelected()) {
				lineChart.getData().add(seriesTotal);
			} else {
				lineChart.getData().remove(seriesTotal);
			}
		});

		btnBack.setOnAction(e -> {
			WindowStatistics windowStatistics = new WindowStatistics(db, selectedData);
			windowStatistics.open(stage, scene.getWidth(), scene.getHeight());
		});
	}
}