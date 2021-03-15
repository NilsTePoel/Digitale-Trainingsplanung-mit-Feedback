package trainingplans.goals;

public class Goal {
	private final int id;
	private final String abbreviation;
	private final String name;

	public Goal(int id, String abbreviation, String name) {
		this.id = id;
		this.abbreviation = abbreviation;
		this.name = name;
	}

	public Goal(String abbreviation, String name) {
		this(-1, abbreviation, name);
	}

	public int getID() {
		return id;
	}

	public String getAbbreviation() {
		return abbreviation;
	}

	public String getName() {
		return name;
	}
}