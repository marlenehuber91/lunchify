package backend.logic;

import backend.interfaces.ConnectionProvider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchService {

    /**
     * Service class for searching users in a database.
     * Uses a {@link ConnectionProvider} to obtain database connections.
     */
    public SearchService() {
    }

    /**
     * Default constructor for the SearchService.
     */
    private static ConnectionProvider connectionProvider;

    /** secures DatabaseConnection */
    public static void setConnectionProvider(ConnectionProvider provider) {
        connectionProvider = provider;
    }

    /**
     * Performs a user search and returns a list of email addresses whose
     * name or email starts with the given query (case-insensitive).
     *
     * @param query the search term (case-insensitive, prefix matching)
     * @return a list of user email addresses (maximum 20 results)
     * @throws SQLException if a database error occurs during the query
     * @throws IllegalStateException if no {@link ConnectionProvider} has been set
     */
    public List<String> searchUsers(String query) throws SQLException {
        if (connectionProvider == null) throw new IllegalStateException("ConnectionProvider ist nicht gesetzt!");

        List<String> results = new ArrayList<>();
        String sql = "SELECT name, email FROM users " +
                "WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? " +
                "ORDER BY name LIMIT 20";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String like = query.toLowerCase() + "%";
            ps.setString(1, like);
            ps.setString(2, like);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String email = rs.getString("email");
                    results.add(email);
                }
            }
        }

        return results;
    }
}
