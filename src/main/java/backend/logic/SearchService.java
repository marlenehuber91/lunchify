package backend.logic;

import backend.interfaces.ConnectionProvider;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchService {

    public SearchService() {
    }

    private static ConnectionProvider connectionProvider;

    public static void setConnectionProvider(ConnectionProvider provider) {
        connectionProvider = provider;
    }

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
