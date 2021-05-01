package trainingplans.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import trainingplans.TrainingPlans;
import trainingplans.evaluations.Evaluation;
import trainingplans.goals.Goal;
import trainingplans.players.Player;
import trainingplans.sessions.Session;
import trainingplans.utils.ErrorMessage;

public class Database {
	private static final int DATABASE_VERSION = 3;
	private final String databaseName;
	private Connection conn;

	public static final Goal[] DEFAULT_GOALS = { new Goal("TE/KOORD", "Techniktraining/Koordinationstraining"), new Goal("TA", "Taktiktraining"),
			new Goal("KT/AT", "Konditionstraining/Athletiktraining"), new Goal("WS", "Wettspiele"), new Goal("IT", "Individualtraining"), new Goal("GT", "Gruppentraining"),
			new Goal("PT", "Positionstraining"), new Goal("TWT", "Torwarttraining"), new Goal("MT", "Mannschaftstraining") };

	public Database(String databaseName) {
		this.databaseName = databaseName;

		connect();
		checkVersion();
		initDatabase();
	}

	private void connect() {
		try {
			conn = DriverManager.getConnection(getURL());
			if (TrainingPlans.debug)
				System.out.println("Verbindung aufgebaut.");
		} catch (SQLException ex) {
			ErrorMessage alert = new ErrorMessage(ex, "Verbindung fehlgeschlagen", "Es konnte keine Verbindung zur Datenbank hergestellt werden:\n" + ex.getMessage());
			alert.show();
		}
	}

	private void checkVersion() {
		int version = getVersion();
		if (version != 0 && version != DATABASE_VERSION) {
			if (version == 2) {
				// Datenbank aktualisieren
				if (TrainingPlans.debug)
					System.out.println("Aktualisiere veraltete Datenbank.");
				executeUpdate(new StringBuilder("ALTER TABLE Trainingseinheiten ADD COLUMN schwerpunkt text"));
			} else {
				Alert alert = new Alert(AlertType.WARNING, "Unbekannte Datenbankversion: " + version + ". Das Programm funktioniert möglicherweise nicht mehr korrekt.");
				alert.setHeaderText("Unbekannte Datenbankversion");
				alert.showAndWait();
			}
		}
	}

	private void initDatabase() {
		StringBuilder querySessions = new StringBuilder();
		querySessions.append("CREATE TABLE IF NOT EXISTS Trainingseinheiten (\n");
		querySessions.append(" id integer PRIMARY KEY,\n");
		querySessions.append(" name text NOT NULL,\n");
		querySessions.append(" schwerpunkt text NOT NULL,\n");
		querySessions.append(" umfang integer NOT NULL,\n");
		querySessions.append(" intensitaet integer NOT NULL,\n");
		querySessions.append(" druckbedingungen integer NOT NULL,\n");
		querySessions.append(" aufmerksamkeit integer NOT NULL,\n");
		querySessions.append(" gesamt integer NOT NULL,\n");
		querySessions.append(" plan text\n");
		querySessions.append(");");
		executeUpdate(querySessions);

		StringBuilder queryPlayers = new StringBuilder();
		queryPlayers.append("CREATE TABLE IF NOT EXISTS Spieler (\n");
		queryPlayers.append(" id integer PRIMARY KEY,\n");
		queryPlayers.append(" name text NOT NULL\n");
		queryPlayers.append(");");
		executeUpdate(queryPlayers);

		StringBuilder queryEvaluations = new StringBuilder();
		queryEvaluations.append("CREATE TABLE IF NOT EXISTS Spielereinschaetzungen (\n");
		queryEvaluations.append(" id integer PRIMARY KEY,\n");
		queryEvaluations.append(" name text NOT NULL,\n");
		queryEvaluations.append(" belastung integer NOT NULL,\n");
		queryEvaluations.append(" ziele text,\n");
		queryEvaluations.append(" spieler_id integer NOT NULL,\n");
		queryEvaluations.append(" FOREIGN KEY (spieler_id) REFERENCES Spieler(id) ON UPDATE CASCADE ON DELETE RESTRICT\n");
		queryEvaluations.append(");");
		executeUpdate(queryEvaluations);

		StringBuilder queryGoals = new StringBuilder();
		queryGoals.append("CREATE TABLE IF NOT EXISTS Trainingsziele (\n");
		queryGoals.append(" id integer PRIMARY KEY,\n");
		queryGoals.append(" abkuerzung text NOT NULL,\n");
		queryGoals.append(" name text NOT NULL\n");
		queryGoals.append(");");
		executeUpdate(queryGoals);

		StringBuilder queryIsGoal = new StringBuilder();
		queryIsGoal.append("CREATE TABLE IF NOT EXISTS IstZiel (\n");
		queryIsGoal.append(" einheit_id integer NOT NULL,\n");
		queryIsGoal.append(" ziel_id integer NOT NULL,\n");
		queryIsGoal.append(" PRIMARY KEY (einheit_id, ziel_id),\n");
		queryIsGoal.append(" FOREIGN KEY (einheit_id) REFERENCES Trainingseinheiten(id) ON UPDATE CASCADE ON DELETE RESTRICT,\n");
		queryIsGoal.append(" FOREIGN KEY (ziel_id) REFERENCES Trainingsziele(id) ON UPDATE CASCADE ON DELETE RESTRICT\n");
		queryIsGoal.append(");");
		executeUpdate(queryIsGoal);

		StringBuilder queryVersion = new StringBuilder();
		queryVersion.append("PRAGMA user_version = ");
		queryVersion.append(DATABASE_VERSION);
		executeUpdate(queryVersion);
	}

	private String getURL() {
		StringBuilder url = new StringBuilder();
		url.append("jdbc:sqlite:");
		url.append(databaseName);
		url.append(".db");

		return url.toString();
	}

	private int getVersion() {
		int version;
		version = (int) executeQuery("PRAGMA user_version", Collections.emptyList(), rs -> {
			rs.next();
			return rs.getInt(1);
		});

		return version;
	}

	private void executeUpdate(StringBuilder query) {
		try (Statement stmt = conn.createStatement()) {
			if (TrainingPlans.debug)
				System.out.println(query);
			stmt.executeUpdate(query.toString());
		} catch (SQLException ex) {
			ErrorMessage alert = new ErrorMessage(ex);
			alert.show();
		}
	}

	private Object executeQuery(String query, List<?> parameters, IResultHandler handler) {
		try (PreparedStatement stmt = conn.prepareStatement(query)) {
			if (TrainingPlans.debug)
				System.out.println(query);
			int parameterIndex = 0;
			for (Object parameter : parameters) {
				stmt.setObject(++parameterIndex, parameter);
			}
			ResultSet rs = stmt.executeQuery();
			return handler.handle(rs);
		} catch (SQLException ex) {
			ErrorMessage alert = new ErrorMessage(ex);
			alert.show();
		}

		return null;
	}

	public String getDatabaseName() {
		return databaseName;
	}

	public void closeConnection() {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException ex) {
			}
		}
	}

	public void searchTableSessions(List<Session> sessions, String searchQuery) {
		sessions.clear();

		if (searchQuery.isBlank()) {
			executeQuery("SELECT * FROM Trainingseinheiten", Collections.emptyList(), rs -> {
				addSession(rs, sessions);
				return null;
			});
		} else {
			executeQuery("SELECT * FROM Trainingseinheiten WHERE name LIKE '%' || ? || '%'", Arrays.asList(searchQuery), rs -> {
				addSession(rs, sessions);
				return null;
			});
		}
	}

	private void addSession(ResultSet rs, List<Session> sessions) throws SQLException {
		while (rs.next()) {
			executeQuery("SELECT Trainingsziele.abkuerzung FROM Trainingsziele JOIN IstZiel ON Trainingsziele.id = IstZiel.ziel_id WHERE IstZiel.einheit_id = ?", Arrays.asList(rs.getString("id")),
					rsGoals -> {
						List<String> goals = new ArrayList<>();
						while (rsGoals.next()) {
							goals.add(rsGoals.getString("abkuerzung"));
						}
						sessions.add(new Session(rs.getInt("id"), rs.getString("name"), rs.getString("schwerpunkt"), LoadDegree.valueOf(rs.getInt("umfang")),
								LoadDegree.valueOf(rs.getInt("intensitaet")), LoadDegree.valueOf(rs.getInt("druckbedingungen")), LoadDegree.valueOf(rs.getInt("aufmerksamkeit")),
								LoadDegree.valueOf(rs.getInt("gesamt")), goals, rs.getString("plan")));
						return null;
					});
		}
	}

	public void searchTablePlayers(List<Player> players, String searchQuery) {
		players.clear();

		if (searchQuery.isBlank()) {
			executeQuery("SELECT * FROM Spieler", Collections.emptyList(), rs -> {
				addPlayer(rs, players);
				return null;
			});
		} else {
			executeQuery("SELECT * FROM Spieler WHERE name LIKE '%' || ? || '%'", Arrays.asList(searchQuery), rs -> {
				addPlayer(rs, players);
				return null;
			});
		}
	}

	private void addPlayer(ResultSet rs, List<Player> players) throws SQLException {
		while (rs.next()) {
			players.add(new Player(rs.getInt("id"), rs.getString("name")));
		}
	}

	public void searchTableEvaluations(List<Evaluation> evaluations, Player player, String searchQuery) {
		evaluations.clear();

		if (searchQuery.isBlank()) {
			executeQuery("SELECT * FROM Spielereinschaetzungen WHERE spieler_id = ?", Arrays.asList(player.getID()), rs -> {
				addEvaluation(rs, evaluations);
				return null;
			});
		} else {
			executeQuery("SELECT * FROM Spielereinschaetzungen WHERE spieler_id = ? AND name LIKE '%' || ? || '%'", Arrays.asList(player.getID(), searchQuery), rs -> {
				addEvaluation(rs, evaluations);
				return null;
			});
		}
	}

	private void addEvaluation(ResultSet rs, List<Evaluation> evaluations) throws SQLException {
		while (rs.next()) {
			evaluations.add(new Evaluation(rs.getInt("id"), rs.getString("name"), LoadDegree.valueOf(rs.getInt("belastung")), rs.getString("ziele")));
		}
	}

	public void searchTableGoals(List<Goal> goals, String searchQuery) {
		goals.clear();

		if (searchQuery.isBlank()) {
			executeQuery("SELECT * FROM Trainingsziele", Collections.emptyList(), rs -> {
				addGoal(rs, goals);
				return null;
			});
		} else {
			executeQuery("SELECT * FROM Trainingsziele WHERE abkuerzung LIKE '%' || ? || '%'", Arrays.asList(searchQuery), rs -> {
				addGoal(rs, goals);
				return null;
			});
		}
	}

	private void addGoal(ResultSet rs, List<Goal> goals) throws SQLException {
		while (rs.next()) {
			goals.add(new Goal(rs.getInt("id"), rs.getString("abkuerzung"), rs.getString("name")));
		}
	}

	public void insertSession(Session session) {
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO Trainingseinheiten (name, schwerpunkt, umfang, intensitaet, druckbedingungen, aufmerksamkeit, gesamt, plan) VALUES('");
		query.append(session.getName());
		query.append("', '");
		query.append(session.getTopic());
		query.append("', '");
		query.append(session.getScope().ordinal());
		query.append("', '");
		query.append(session.getIntensity().ordinal());
		query.append("', '");
		query.append(session.getPressure().ordinal());
		query.append("', '");
		query.append(session.getAttention().ordinal());
		query.append("', '");
		query.append(session.getTotal().ordinal());
		query.append("', '");
		query.append(session.getPlan());
		query.append("')");
		executeUpdate(query);

		// Ziele zuordnen
		int id = getSessionID(session.getName());
		for (String goal : session.getGoals()) {
			StringBuilder queryGoals = new StringBuilder();
			queryGoals.append("INSERT INTO IstZiel (einheit_id, ziel_id) VALUES('");
			queryGoals.append(id);
			queryGoals.append("', '");
			queryGoals.append(getGoalID(goal));
			queryGoals.append("')");
			executeUpdate(queryGoals);
		}
	}

	public void insertPlayer(Player player) {
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO Spieler (name) VALUES('");
		query.append(player.getName());
		query.append("')");
		executeUpdate(query);
	}

	public void insertEvaluation(Evaluation evaluation, Player player) {
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO Spielereinschaetzungen (name, belastung, ziele, spieler_id) VALUES('");
		query.append(evaluation.getName());
		query.append("', '");
		query.append(evaluation.getLoad().ordinal());
		query.append("', '");
		query.append(evaluation.getGoals());
		query.append("', '");
		query.append(player.getID());
		query.append("')");
		executeUpdate(query);
	}

	public void insertGoal(Goal goal) {
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO Trainingsziele (abkuerzung, name) VALUES('");
		query.append(goal.getAbbreviation());
		query.append("', '");
		query.append(goal.getName());
		query.append("')");
		executeUpdate(query);
	}

	public void insertDefaultGoals() {
		executeUpdate(new StringBuilder("DELETE FROM Trainingsziele")); // Bestehende Ziele löschen

		for (Goal goal : DEFAULT_GOALS) {
			insertGoal(goal);
		}
	}

	public void updateSession(Session session) {
		StringBuilder query = new StringBuilder();
		query.append("UPDATE Trainingseinheiten SET name = '");
		query.append(session.getName());
		query.append("', schwerpunkt = '");
		query.append(session.getTopic());
		query.append("', umfang = '");
		query.append(session.getScope().ordinal());
		query.append("', intensitaet = '");
		query.append(session.getIntensity().ordinal());
		query.append("', druckbedingungen = '");
		query.append(session.getPressure().ordinal());
		query.append("', aufmerksamkeit = '");
		query.append(session.getAttention().ordinal());
		query.append("', gesamt = '");
		query.append(session.getTotal().ordinal());
		query.append("', plan = '");
		query.append(session.getPlan());
		query.append("' WHERE id = ");
		query.append(session.getID());
		executeUpdate(query);

		// Bisher zugeordnete Ziele löschen
		StringBuilder queryDeleteGoals = new StringBuilder();
		queryDeleteGoals.append("DELETE FROM IstZiel WHERE einheit_id = ");
		queryDeleteGoals.append(session.getID());
		executeUpdate(queryDeleteGoals);

		// Ziele neu zuordnen
		for (String goal : session.getGoals()) {
			StringBuilder queryGoals = new StringBuilder();
			queryGoals.append("INSERT INTO IstZiel (einheit_id, ziel_id) VALUES('");
			queryGoals.append(session.getID());
			queryGoals.append("', '");
			queryGoals.append(getGoalID(goal));
			queryGoals.append("')");
			executeUpdate(queryGoals);
		}
	}

	public void updatePlayer(Player player) {
		StringBuilder query = new StringBuilder();
		query.append("UPDATE Spieler SET name = '");
		query.append(player.getName());
		query.append("' WHERE id = ");
		query.append(player.getID());
		executeUpdate(query);
	}

	public void updateEvaluation(Evaluation evaluation) {
		StringBuilder query = new StringBuilder();
		query.append("UPDATE Spielereinschaetzungen SET name = '");
		query.append(evaluation.getName());
		query.append("', belastung = '");
		query.append(evaluation.getLoad().ordinal());
		query.append("', ziele = '");
		query.append(evaluation.getGoals());
		query.append("' WHERE id = ");
		query.append(evaluation.getID());
		executeUpdate(query);
	}

	public void updateGoal(Goal goal) {
		StringBuilder query = new StringBuilder();
		query.append("UPDATE Trainingsziele SET abkuerzung = '");
		query.append(goal.getAbbreviation());
		query.append("', name = '");
		query.append(goal.getName());
		query.append("' WHERE id = ");
		query.append(goal.getID());
		executeUpdate(query);
	}

	public void removeSession(Session session) {
		// Alle zugeordneten Ziele löschen
		StringBuilder queryDeleteGoals = new StringBuilder();
		queryDeleteGoals.append("DELETE FROM IstZiel WHERE einheit_id = ");
		queryDeleteGoals.append(session.getID());
		executeUpdate(queryDeleteGoals);

		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM Trainingseinheiten WHERE id = ");
		query.append(session.getID());
		executeUpdate(query);
	}

	public void removePlayer(Player player) {
		// Alle dem Spieler zugeordneten Einschätzungen löschen
		StringBuilder queryDeleteEvaluations = new StringBuilder();
		queryDeleteEvaluations.append("DELETE FROM Spielereinschaetzungen WHERE spieler_id = ");
		queryDeleteEvaluations.append(player.getID());
		executeUpdate(queryDeleteEvaluations);

		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM Spieler WHERE id = ");
		query.append(player.getID());
		executeUpdate(query);
	}

	public void removeEvaluation(Evaluation evaluation) {
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM Spielereinschaetzungen WHERE id = ");
		query.append(evaluation.getID());
		executeUpdate(query);
	}

	public void removeGoal(Goal goal) {
		StringBuilder query = new StringBuilder();
		query.append("DELETE FROM Trainingsziele WHERE id = ");
		query.append(goal.getID());
		executeUpdate(query);
	}

	public ArrayList<String> getGoals() {
		ArrayList<String> goals = new ArrayList<>();

		executeQuery("SELECT abkuerzung FROM Trainingsziele", Collections.emptyList(), rs -> {
			while (rs.next()) {
				goals.add(rs.getString("abkuerzung"));
			}
			return null;
		});

		return goals;
	}

	// Wie oft kommt das angegebene Ziel in den ausgewählten Trainingseinheiten vor?
	public int getGoalCount(List<Session> selectedData, String goal) {
		if (selectedData.isEmpty())
			return 0;

		try (Statement stmt = conn.createStatement()) {
			StringBuilder query = new StringBuilder();
			query.append("SELECT COUNT(ziel_id) FROM IstZiel WHERE ziel_id = ");
			query.append(getGoalID(goal));
			query.append(" AND einheit_id IN (");
			for (Session data : selectedData) {
				query.append(data.getID());
				query.append(", ");
			}
			query.delete(query.length() - 2, query.length()); // Letztes Komma entfernen
			query.append(")");
			if (TrainingPlans.debug)
				System.out.println(query);

			ResultSet rs = stmt.executeQuery(query.toString());
			rs.next();
			int count = Integer.parseInt(rs.getString("COUNT(ziel_id)"));

			return count;
		} catch (SQLException ex) {
			ErrorMessage alert = new ErrorMessage(ex);
			alert.show();
		}

		return 0;
	}

	// Gibt die ID der ersten Einheit mit dem angegebenen Namen zurück
	private int getSessionID(String name) {
		int sessionID;

		sessionID = (int) executeQuery("SELECT id FROM Trainingseinheiten WHERE name = ?", Arrays.asList(name), rs -> {
			rs.next();
			return rs.getInt("id");
		});

		return sessionID;
	}

	// Gibt die ID des ersten Ziels mit dem angegebenen Namen zurück
	private int getGoalID(String name) {
		int goalID;

		goalID = (int) executeQuery("SELECT id FROM Trainingsziele WHERE abkuerzung = ?", Arrays.asList(name), rs -> {
			rs.next();
			return rs.getInt("id");
		});

		return goalID;
	}
}
