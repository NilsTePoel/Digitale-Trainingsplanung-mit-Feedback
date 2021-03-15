package trainingplans.database;

import java.sql.ResultSet;
import java.sql.SQLException;

public interface IResultHandler {
	Object handle(ResultSet rs) throws SQLException;
}
