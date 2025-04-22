package backend.logic;

import database.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SearchService {

    public List<String> searchEmails(String query) {
        List<String> emails = new ArrayList<>();
        String sql = "SELECT email FROM users WHERE email LIKE ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, query + "%");

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                emails.add(rs.getString("email"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return emails;
    }
}
