package trainingplans.sessions;

import java.util.List;

import trainingplans.database.LoadDegree;

public class Session {
	private final int id;
	private final String name;
	private final String topic;
	private final LoadDegree scope;
	private final LoadDegree intensity;
	private final LoadDegree pressure;
	private final LoadDegree attention;
	private final LoadDegree total;
	private final List<String> goals;
	private final String plan;

	public Session(int id, String name, String topic, LoadDegree scope, LoadDegree intensity, LoadDegree pressure, LoadDegree attention, LoadDegree total, List<String> goals, String plan) {
		this.id = id;
		this.name = name;
		this.topic = topic;
		this.scope = scope;
		this.intensity = intensity;
		this.pressure = pressure;
		this.attention = attention;
		this.total = total;
		this.goals = goals;
		this.plan = plan;
	}

	public Session(String name, String topic, LoadDegree scope, LoadDegree intensity, LoadDegree pressure, LoadDegree attention, LoadDegree total, List<String> goals, String plan) {
		this(-1, name, topic, scope, intensity, pressure, attention, total, goals, plan);
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getTopic() {
		return topic;
	}

	public LoadDegree getScope() {
		return scope;
	}

	public LoadDegree getIntensity() {
		return intensity;
	}

	public LoadDegree getPressure() {
		return pressure;
	}

	public LoadDegree getAttention() {
		return attention;
	}

	public LoadDegree getTotal() {
		return total;
	}

	public List<String> getGoals() {
		return goals;
	}

	public String getPlan() {
		return plan;
	}
}