package trainingplans.database;

public enum LoadDegree {
	I, II, III, IV, V;

	public static LoadDegree valueOf(int value) {
		switch (value) {
		case 0:
			return I;
		case 1:
			return II;
		case 2:
			return III;
		case 3:
			return IV;
		default:
			return V;
		}
	}
}
