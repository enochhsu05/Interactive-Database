import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.*;

public class DatabaseController {
    private static final String DATABASE_URL = "jdbc:sqlite:anime_database.db";

    public static void closeDatabase(Connection conn) {
        try {
            conn.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static Connection connectToDatabase() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(DATABASE_URL);
            System.out.println("Connection to SQLite has been established.");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return conn;
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

    private static void queryAndPrintMovies(Connection conn) {
        String query = "SELECT anime_name AS movie_name FROM Movie";
        try (PreparedStatement sqlStatement = conn.prepareStatement(query);
             ResultSet rs = sqlStatement.executeQuery()) {
            while (rs.next()) {
                System.out.println(rs.getString("movie_name"));
            }
        } catch (Exception e) {
            System.out.println("Error querying movies: " + e.getMessage());
        }
    }
}
