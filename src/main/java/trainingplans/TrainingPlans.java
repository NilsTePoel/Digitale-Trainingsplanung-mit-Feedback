package trainingplans;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import trainingplans.database.Database;
import trainingplans.evaluations.WindowAddMultipleEvaluations;
import trainingplans.goals.WindowGoals;
import trainingplans.players.Player;
import trainingplans.players.WindowPlayers;
import trainingplans.sessions.WindowSessions;
import trainingplans.statistics.WindowSelectSessions;
import trainingplans.utils.ErrorMessage;
import trainingplans.utils.FileAccess;

public class TrainingPlans extends Application {
	private static final String VERSION = "1.1.0";
	private static final String BUILD_DATE = "01.05.2021";
	private static final Path SETTINGS_FILE = Path.of("Einstellungen.txt");
	private static final String DOCUMENTATION_FILE = "Dokumentation.pdf";
	private static final String TOPICS_FILE = "Trainingsschwerpunkte und technische Elemente.pdf";
	public static boolean debug = false;

	private Database db;

	public TrainingPlans() {
	}

	public TrainingPlans(Database db) {
		this.db = db;
	}

	public void open(Stage stage, double width, double height) {
		stage.setTitle("Digitale Trainingsplanung mit Feedback (v" + VERSION + ")");

		BorderPane borderPane = new BorderPane();

		Menu menuDatabase = new Menu("Datenbank");
		MenuItem itemChangeDatabaseName = new MenuItem("Datenbankname ??ndern ...");
		menuDatabase.getItems().add(itemChangeDatabaseName);

		Menu menuHelp = new Menu("Hilfe");
		MenuItem itemDocumentation = new MenuItem("Dokumentation");
		itemDocumentation.setDisable(!new File(DOCUMENTATION_FILE).exists());
		MenuItem itemInfo = new MenuItem("Info");
		menuHelp.getItems().addAll(itemDocumentation, itemInfo);

		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().addAll(menuDatabase, menuHelp);
		borderPane.setTop(menuBar);

		GridPane gridActions = new GridPane();
		gridActions.setAlignment(Pos.CENTER);
		gridActions.setVgap(10);

		GridPane gridTitle = new GridPane();
		gridTitle.setAlignment(Pos.CENTER);
		Text title = new Text("Digitale Trainingsplanung mit Feedback");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		gridTitle.add(title, 0, 0);
		gridActions.add(gridTitle, 0, 0);

		Button btnSessions = new Button("Trainingseinheiten und Trainereinsch??tzungen");
		btnSessions.setMaxWidth(Double.MAX_VALUE);
		gridActions.add(btnSessions, 0, 1);

		Button btnEvaluations = new Button("Spielereinsch??tzungen");
		btnEvaluations.setMaxWidth(Double.MAX_VALUE);
		gridActions.add(btnEvaluations, 0, 2);

		Button btnAddMultipleEvaluations = new Button("Mehrere Spielereinsch??tzungen hinzuf??gen");
		btnAddMultipleEvaluations.setMaxWidth(Double.MAX_VALUE);
		gridActions.add(btnAddMultipleEvaluations, 0, 3);

		Button btnGoals = new Button("Trainingsziele");
		btnGoals.setMaxWidth(Double.MAX_VALUE);
		gridActions.add(btnGoals, 0, 4);

		Button btnStatistics = new Button("Statistiken");
		btnStatistics.setMaxWidth(Double.MAX_VALUE);
		gridActions.add(btnStatistics, 0, 5);

		Button btnTopics = new Button("Auswahl der Trainingsschwerpunkte und technischen Elemente");
		btnTopics.setMaxWidth(Double.MAX_VALUE);
		btnTopics.setDisable(!new File(TOPICS_FILE).exists());
		gridActions.add(btnTopics, 0, 6);

		borderPane.setCenter(gridActions);

		Scene scene = new Scene(borderPane, width, height);
		stage.setScene(scene);
		stage.show();

		itemChangeDatabaseName.setOnAction(e -> {
			TextInputDialog input = new TextInputDialog(db.getDatabaseName());
			input.setHeaderText("Datenbankname");
			Optional<String> result = input.showAndWait();
			if (result.isPresent()) {
				String databaseName = result.get();
				db.closeConnection();
				db = new Database(databaseName);
				writeDatabaseName(databaseName);
			}
		});

		itemDocumentation.setOnAction(e -> {
			FileAccess documentation = new FileAccess(DOCUMENTATION_FILE, "Die Dokumentation");
			documentation.open();
		});

		itemInfo.setOnAction(e -> {
			StringBuilder message = new StringBuilder();
			message.append("Digitale Trainingsplanung mit Feedback (v");
			message.append(VERSION);
			message.append(", ");
			message.append(BUILD_DATE);
			message.append(")\nNils te Poel unter Mitarbeit von Hans-Dieter te Poel\n\nBetriebssystem: ");
			message.append(System.getProperty("os.name"));
			message.append(" (");
			message.append(System.getProperty("os.version"));
			message.append(")\nJava-Version: ");
			message.append(System.getProperty("java.version"));
			message.append("\n\nVerwendete Bibliotheken:\n * JavaFX\n    - Website: https://openjfx.io\n    - Lizenz: https://github.com/openjdk/jfx/blob/master/LICENSE (GPL)");
			message.append("\n * sqlite-jdbc\n    - Website: https://github.com/xerial/sqlite-jdbc\n    - Lizenz: https://github.com/xerial/sqlite-jdbc/blob/master/LICENSE (Apache License)");

			TextArea taMessage = new TextArea(message.toString());
			taMessage.setEditable(false);
			taMessage.setWrapText(true);
			GridPane gridMessage = new GridPane();
			gridMessage.setMaxWidth(Double.MAX_VALUE);
			gridMessage.add(taMessage, 0, 0);

			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setHeaderText("Info");
			alert.getDialogPane().setContent(gridMessage);
			alert.showAndWait();
		});

		btnSessions.setOnAction(e -> {
			WindowSessions windowSessions = new WindowSessions(db);
			windowSessions.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnEvaluations.setOnAction(e -> {
			WindowPlayers windowPlayers = new WindowPlayers(db);
			windowPlayers.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnAddMultipleEvaluations.setOnAction(e -> {
			List<Player> players = new ArrayList<>();
			db.searchTablePlayers(players, "");
			if (players.isEmpty()) {
				Alert alert = new Alert(AlertType.ERROR, "Es wurden noch keine Spieler hinzugef??gt.\nBitte legen Sie erst unter dem Men??punkt \"Spielereinsch??tzungen\" die Spieler an.");
				alert.setHeaderText("Keine Spieler");
				alert.showAndWait();
			} else {
				WindowAddMultipleEvaluations windowAddMultipleEvaluations = new WindowAddMultipleEvaluations(db, players);
				windowAddMultipleEvaluations.open(stage);
			}
		});

		btnGoals.setOnAction(e -> {
			WindowGoals windowGoals = new WindowGoals(db);
			windowGoals.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnStatistics.setOnAction(e -> {
			WindowSelectSessions windowSelectSessions = new WindowSelectSessions(db);
			windowSelectSessions.open(stage, scene.getWidth(), scene.getHeight());
		});

		btnTopics.setOnAction(e -> {
			FileAccess topicsList = new FileAccess(TOPICS_FILE, "Die Liste der Trainingsschwerpunkte und technischen Elemente");
			topicsList.open();
		});
	}

	@Override
	public void start(Stage primaryStage) {
		db = new Database(readDatabaseName());
		open(primaryStage, 700, 450);
	}

	public static void main(String[] args) {
		for (String argument : args) {
			if (argument.equals("debug")) {
				debug = true;
				System.out.println("Debug-Modus aktiviert.");
			}
		}

		launch(args);
	}

	private String readDatabaseName() {
		String databaseName = "Trainingsplanung";
		try (BufferedReader bufferedReader = Files.newBufferedReader(SETTINGS_FILE)) {
			String databaseSetting = bufferedReader.readLine();

			// Liegt ein g??ltiger Name vor?
			if (databaseSetting != null && !databaseSetting.isBlank()) {
				databaseName = databaseSetting;
			}
		} catch (IOException ex) {
		} // Der Standard-Datenbankname wurde noch nicht ge??ndert oder die
			// Einstellungsdatei ist fehlerhaft

		return databaseName;
	}

	private void writeDatabaseName(String databaseName) {
		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(SETTINGS_FILE)) {
			bufferedWriter.write(databaseName);
		} catch (IOException ex) {
			ErrorMessage alert = new ErrorMessage(ex, "Schreiben der Datei fehlgeschlagen", "Der Datenbankname konnte nicht gespeichert werden:\n" + ex.getMessage());
			alert.showMessage();
		}
	}
}