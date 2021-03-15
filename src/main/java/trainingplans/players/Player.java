package trainingplans.players;

public class Player {
	private final int id;
	private final String name;

	public Player(int id, String name) {
		this.id = id;
		this.name = name;
	}

	public Player(String name) {
		this(-1, name);
	}

	public int getID() {
		return id;
	}

	public String getName() {
		return name;
	}
}