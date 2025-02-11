import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class handles all database related transactions
 */
public class DatabaseConnectionHandler {

	private static final String DATABASE_URL = "jdbc:sqlite:anime_database.db";

	public DatabaseConnectionHandler() {
		try {
			connection = connectToDatabase();
			initialPopulation(connection);
			nestedGroupByStudioCountAnime();
			connection.setAutoCommit(false);
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	public static void initialPopulation(Connection conn) {
		String path = "initial population.sql";
		try (BufferedReader br = new BufferedReader(new FileReader(path));
			 Statement stmt = conn.createStatement()) {
			String line;
			StringBuilder sql = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sql.append(line).append("\n");
				if (line.trim().endsWith(";")) {
					stmt.execute(sql.toString());
					sql.setLength(0);
				}
			}
		} catch (Exception e) {
			System.out.println("Error executing SQL file: " + e.getMessage());
		}
	}


	private static void executeSqlFromFile(Connection conn, String filePath) {
		try (BufferedReader br = new BufferedReader(new FileReader(filePath));
			 Statement stmt = conn.createStatement()) {
			String line;
			StringBuilder sql = new StringBuilder();
			while ((line = br.readLine()) != null) {
				sql.append(line).append("\n");
				if (line.trim().endsWith(";")) {
					stmt.execute(sql.toString());
					sql.setLength(0);
				}
			}
		} catch (Exception e) {
			System.out.println("Error executing SQL file: " + e.getMessage());
		}
	}

	private static Connection connectToDatabase() {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(DATABASE_URL);
			System.out.println("Connection to SQLite has been established.");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
		return connection;
	}


	private static final String EXCEPTION_TAG = "[EXCEPTION]";
	private static Connection connection = null;


	public void close() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException e) {
			System.out.println(EXCEPTION_TAG + " " + e.getMessage());
		}
	}

	private static void rollbackConnection() {
		try {
			connection.rollback();
		} catch (SQLException e) {
			System.out.println(EXCEPTION_TAG + " " + e.getMessage());
		}
	}


	public static boolean insertAnime(String name, int author_id, String studio, String genre, int year_aired, String status) throws Exception {
		Boolean result = false;
		try {
			System.out.println(connection);
			String query = "INSERT INTO Anime SELECT ?,?,?,?,?,? " +
					"WHERE EXISTS (SELECT * FROM Author WHERE id = ?) " +
					"AND EXISTS (SELECT * FROM Genre WHERE name = ?)";
			PrintablePreparedStatement s = new PrintablePreparedStatement(connection.prepareStatement(query), query, false);
			s.setString(1, name);
			s.setInt(2, author_id);
			s.setString(3, studio);
			s.setString(4, genre);
			s.setInt(5, year_aired);
			s.setString(6, status);
			s.setInt(7, author_id);
			s.setString(8, genre);
			insertStudio(studio);
			int rowCount = s.executeUpdate();
			if (rowCount == 0) {
				removeStudio(studio);
				System.out.println(EXCEPTION_TAG + " Given Author and/or Genre do not exist");
				throw (new Exception("Given Author and/or Genre do not exist"));
			}
			result = true;
		} catch (SQLException e) {
			System.out.println(EXCEPTION_TAG + " " + e.getMessage());
			rollbackConnection();
			throw e;
		} catch (Exception e) {
			throw e;
		}
		return result;
	}


	public static void insertStudio(String name) throws SQLException {
		try {
			String query = "INSERT INTO Studio SELECT ?, null, null " +
					"WHERE NOT EXISTS (SELECT * FROM Studio WHERE name = ?)";
			PrintablePreparedStatement s = new PrintablePreparedStatement(connection.prepareStatement(query), query, false);
			s.setString(1, name);
			s.setString(2, name);

			int rowCount = s.executeUpdate();
			if (rowCount != 0) {
				System.out.println("Studio " + name + " is added to Studios");
			}
			connection.commit();
			s.close();
		} catch (SQLException e) {
			System.out.println(EXCEPTION_TAG + " " + e.getMessage());
			rollbackConnection();
			throw e;
		}
	}


	public static boolean updateAnime(String name, int author_id, String studio, String genre, int year_aired, String status) throws SQLException {
		Boolean result = false;
		try {
			String query = "UPDATE anime SET author_id = ?, studio = ?, genre = ?, year_aired = ?, status = ? " +
					"WHERE name = ?";
			PrintablePreparedStatement s = new PrintablePreparedStatement(connection.prepareStatement(query), query, false);
			s.setInt(1, author_id);
			s.setString(2, studio);
			s.setString(3, genre);
			s.setInt(4, year_aired);
			s.setString(5, status);
			s.setString(6, name);

			int rowCount = s.executeUpdate();
			if (rowCount == 0) {
				System.out.println("Anime " + name + " does not exist");
			} else {
				result = true;
			}
			connection.commit();
			s.close();
		} catch (SQLException e) {
			System.out.println(EXCEPTION_TAG + " " + e.getMessage());
			rollbackConnection();
			throw e;
		}
		return result;
	}

	public static void deleteAnime(String anime_name) {
		try {
			String query = "DELETE FROM Anime WHERE name = ?";
			PrintablePreparedStatement s = new PrintablePreparedStatement(connection.prepareStatement(query), query, false);
			s.setString(1, anime_name);
			int rowCount = s.executeUpdate();
			if (rowCount == 0) {
				System.out.println("Anime " + anime_name + " does not exist");
			}
			connection.commit();

			s.close();
		} catch (SQLException e) {
			System.out.println(EXCEPTION_TAG + " " + e.getMessage());
			rollbackConnection();
		}
	}

	// Display how many anime of each genre is in the database.
	public Vector<Vector<String>> groupByGenreCountAnime() {
		Vector<Vector<String>> result = new Vector<>();
		try {
			String query = "SELECT genre, COUNT(name) FROM Anime GROUP BY genre";
			PrintablePreparedStatement s = new PrintablePreparedStatement(connection.prepareStatement(query), query, false);
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				Vector<String> row = new Vector<>();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					row.add(rs.getString(i));
				}
				result.add(row);
			}
			connection.commit();
			s.close();
		} catch (SQLException e) {
			System.out.println(EXCEPTION_TAG + " " + e.getMessage());
			rollbackConnection();
		}
		return result;
	}

	public Vector<Vector<String>> nestedGroupByStudioCountAnime() {
		Vector<Vector<String>> result = new Vector<>();
		try {
			String query = "SELECT studio, COUNT(name) FROM Anime WHERE year_aired > (SELECT MAX(year_aired) - 5 " +
					"FROM Anime) GROUP BY studio";
			PrintablePreparedStatement s = new PrintablePreparedStatement(connection.prepareStatement(query), query, false);
			ResultSet rs = s.executeQuery();
			while (rs.next()) {
				Vector<String> row = new Vector<>();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					row.add(rs.getString(i));
				}
				result.add(row);
			}
			connection.commit();
			s.close();
		} catch (SQLException e) {
			System.out.println(EXCEPTION_TAG + " " + e.getMessage());
			rollbackConnection();
		}
		return result;
	}

	// Selection
	public Vector<Vector<String>> filterAnime(String input) {
		String[] clauses = input.split("(?i)\\s+(AND|OR)\\s+");
		List<String> clauseList = new ArrayList<>();
		List<String> numericalOperators = Arrays.asList("=", ">", "<", ">=", "<=");
		for (String clause : clauses) {
			if (!clause.equals("'")) {
				clause = clause.contains(" ") ? clause.split(" ")[0].toLowerCase() + clause.substring(clause.indexOf(" ")) : clause;
				clause = clause.replaceAll("(?i)year aired", "year_aired");
				clause = clause.replaceAll("(?i)author id", "author_id");
				clause = clause.trim();
				String[] splitClause = clause.split(" ");
				if (splitClause.length < 3) {
					throw new IllegalArgumentException("Invalid clause syntax: " + clause);
				} else if (splitClause.length > 3) {
					for (int i = 3; i < splitClause.length; i++) {
						splitClause[2] += " " + splitClause[i];
					}
				}
				switch (splitClause[0]) {
					case "name":
					case "studio":
					case "genre":
					case "status":
						if (!splitClause[1].equals("=")) {
							throw new IllegalArgumentException("Invalid clause syntax: " + clause);
						}
						if (splitClause[2].indexOf("'") != 0 || splitClause[2].lastIndexOf("'") != splitClause[2].length() - 1) {
							throw new IllegalArgumentException("Use single quotations: " + clause);
						}
						break;
					case "author_id":
					case "year_aired":
						if (!numericalOperators.contains(splitClause[1])) {
							throw new IllegalArgumentException("Invalid operator: " + clause);
						}
						try {
							Integer.parseInt(splitClause[2]);
						} catch (NumberFormatException e) {
							throw new IllegalArgumentException("Provide the author id: " + clause);
						}
						break;
					default:
						throw new IllegalArgumentException("Invalid anime attribute");
				}
				clauseList.add(clause);
			}
		}
		List<String> operators = new ArrayList<>();
		Pattern operatorPattern = Pattern.compile("(?i)\\s+(AND|OR)\\s+");
		Matcher matcher = operatorPattern.matcher(input);
		while (matcher.find()) {
			operators.add(matcher.group(1).toUpperCase());
		}
		if (!clauseList.isEmpty() && clauseList.size() - operators.size() != 1) {
			throw new IllegalArgumentException("Invalid query");
		}
		String query = "SELECT * FROM Anime";
		if (!clauseList.isEmpty()) {
			query += " WHERE ";
			for (int i = 0; i < operators.size(); i++) {
				query += clauseList.get(0) + " " + operators.get(i) + " ";
			}
			query += clauseList.get(clauseList.size() - 1);
		}
		Vector<Vector<String>> result = new Vector<>();
		try (PreparedStatement sqlStatement = connection.prepareStatement(query);
			 ResultSet rs = sqlStatement.executeQuery()) {
			while (rs.next()) {
				Vector<String> row = new Vector<>();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					row.add(rs.getString(i));
				}
				result.add(row);
			}
		} catch (Exception e) {
			System.out.println("Error projecting anime: " + e.getMessage());
		}
		return result;
	}

	// Projection
	public Vector<Vector<String>> getAnimeColumns(Boolean name, Boolean author, Boolean studio, Boolean genre, Boolean year, Boolean status) {
		String sqlName = name ? "name, " : "";
		String sqlAuthor = author ? "author_id, " : "";
		String sqlStudio = studio ? "studio, " : "";
		String sqlGenre = genre ? "genre, " : "";
		String sqlYear = year ? "year_aired, " : "";
		String sqlStatus = status ? "status" : "";
		String params = sqlName + sqlAuthor + sqlStudio + sqlGenre + sqlYear + sqlStatus;
		if (params.lastIndexOf(" ") == params.length() - 1) {
			params = params.substring(0, params.length() - 2);
		}
		String query = "SELECT " + params + " FROM Anime";
		Vector<Vector<String>> result = new Vector<>();
		try (PreparedStatement sqlStatement = connection.prepareStatement(query);
			 ResultSet rs = sqlStatement.executeQuery()) {
			while (rs.next()) {
				Vector<String> row = new Vector<>();
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					row.add(rs.getString(i));
				}
				result.add(row);
			}
		} catch (Exception e) {
			System.out.println("Error projecting anime: " + e.getMessage());
		}
		return result;
	}

	// Join
	public Vector<String> getAnimeByStudio(String studio) {
		String query = "SELECT Studio.name AS studio_name, Anime.name\n" +
				"FROM Studio\n" +
				"JOIN Anime ON Studio.name = Anime.studio\n" +
				"WHERE Studio.name = '" +
				studio + "'";
		try (PreparedStatement sqlStatement = connection.prepareStatement(query);
			 ResultSet rs = sqlStatement.executeQuery()) {
			Vector<String> result = new Vector<>();
			while (rs.next()) {
				result.add(rs.getString(2));
			}
			return result;
		} catch (Exception e) {
			System.out.println("Error projecting anime: " + e.getMessage());
		}
		return new Vector<>();
	}

	// Aggregation with Having
	public Vector<String> getStudiosWithMultipleAnime() {
		String query = "SELECT studio, COUNT(*) AS anime_count\n" +
				"FROM Anime\n" +
				"GROUP BY studio\n" +
				"HAVING COUNT(*) >= 2;";
		try (PreparedStatement sqlStatement = connection.prepareStatement(query);
			 ResultSet rs = sqlStatement.executeQuery()) {
			Vector<String> result = new Vector<>();
			while (rs.next()) {
				result.add(rs.getString(1));
			}
			return result;
		} catch (Exception e) {
			System.out.println("Error projecting anime: " + e.getMessage());
		}
		return new Vector<>();
	}

	// Division
	public Vector<String> getAnimeReviewedByAllUsers() {
		String query = "SELECT anime\n" +
				"FROM Review\n" +
				"GROUP BY anime\n" +
				"HAVING COUNT(DISTINCT user) = (SELECT COUNT(*) FROM User);";
		try (PreparedStatement sqlStatement = connection.prepareStatement(query);
			 ResultSet rs = sqlStatement.executeQuery()) {
			Vector<String> result = new Vector<>();
			while (rs.next()) {
				result.add(rs.getString(1));
			}
			if (result.isEmpty()) {
				System.out.println("Broken");
			}
			return result;
		} catch (Exception e) {
			System.out.println("Error projecting anime: " + e.getMessage());
		}
		return new Vector<>();
	}

	public Vector<String> printAnime() {
		String query = "SELECT * FROM Anime";
		Vector<String> result = new Vector<>();
		try (PreparedStatement sqlStatement = connection.prepareStatement(query);
			 ResultSet rs = sqlStatement.executeQuery()) {
			while (rs.next()) {
				result.add(rs.getString(1));
			}
			if (result.isEmpty()) {
				System.out.println("Broken");
			}
			return result;
		} catch (Exception e) {
			System.out.println("Error querying movies: " + e.getMessage());
		}
        return result;
    }

	public Vector<String> getAnimeAttributes(String anime_name) {
		String query = "SELECT * FROM Anime WHERE name = '" + anime_name + "'";
		System.out.println(query);
		try (PreparedStatement sqlStatement = connection.prepareStatement(query);
			 ResultSet rs = sqlStatement.executeQuery()) {
			Vector<String> result = new Vector<>();
			while (rs.next()) {
				for (int i = 2; i <= rs.getMetaData().getColumnCount(); i++) {
					result.add(rs.getString(i));
				}
			}
			return result;
		} catch (Exception e) {
			System.out.println("Error projecting anime: " + e.getMessage());
		}
		return new Vector<>();
	}

	private static void removeStudio(String studio_name) {
		try {
			String query = "DELETE FROM Studio WHERE name = ?";
			PrintablePreparedStatement s = new PrintablePreparedStatement(connection.prepareStatement(query), query, false);
			s.setString(1, studio_name);
			int rowCount = s.executeUpdate();
			if (rowCount == 0) {
				System.out.println("Studio " + studio_name + " does not exist");
			}
			connection.commit();

			s.close();
		} catch (SQLException e) {
			System.out.println(EXCEPTION_TAG + " " + e.getMessage());
			rollbackConnection();
		}
	}
}
