import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Queries {
    private Connection conn;
    Queries() {
        conn = DatabaseController.connectToDatabase();
        DatabaseController.initialPopulation(conn);
    }

    public void closeDatabase() {
        DatabaseController.closeDatabase(conn);
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
                        if (!splitClause[1].equals("=")) {
                            throw new IllegalArgumentException("Invalid clause syntax: " + clause);
                        }
                        try {
                            Integer.parseInt(splitClause[2]);
                        } catch (NumberFormatException e) {
                            throw new IllegalArgumentException("Provide the author id: " + clause);
                        }
                        break;
                    case "status":
                        List<String> statuses = Arrays.asList("aired", "airing", "unaired");
                        if (!statuses.contains(splitClause[2])) {
                            throw new IllegalArgumentException("Invalid status: " + clause);
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
        try (PreparedStatement sqlStatement = conn.prepareStatement(query);
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
        try (PreparedStatement sqlStatement = conn.prepareStatement(query);
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
        System.out.println(query);
        try (PreparedStatement sqlStatement = conn.prepareStatement(query);
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
        try (PreparedStatement sqlStatement = conn.prepareStatement(query);
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
        try (PreparedStatement sqlStatement = conn.prepareStatement(query);
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

    public Vector<String> getAnimeAttributes(String anime_name) {
        String query = "SELECT * FROM Anime WHERE name = '" + anime_name + "'";
        System.out.println(query);
        try (PreparedStatement sqlStatement = conn.prepareStatement(query);
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
}
