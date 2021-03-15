package trainingplans.evaluations;

import trainingplans.database.LoadDegree;

public class Evaluation {
	private final int id;
	private final String name;
	private final LoadDegree load;
	private final String goals;

	public Evaluation(int id, String name, LoadDegree load, String goals) {
		this.id = id;
		this.name = name;
		this.load = load;
		this.goals = goals;
	}

	public Evaluation(String name, LoadDegree load, String goals) {
		this(-1, name, load, goals);
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public LoadDegree getLoad() {
		return load;
	}

	public String getGoals() {
		return goals;
	}
}