package backend.logic;

import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import database.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

public class UserService {

    public static UserRole authenticate(String email, String password) {
        String query = "SELECT id, name, email, password, role, state FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPasswordHash = rs.getString("password");
                if (BCrypt.checkpw(password, storedPasswordHash)) {
                    User authenticatedUser = new User(
                            rs.getString("name"),
                            rs.getString("email"),
                            storedPasswordHash,
                            UserRole.valueOf(rs.getString("role").toUpperCase()),
                            UserState.valueOf(rs.getString("state").toUpperCase())
                    );
                    return authenticatedUser.getRole();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // TODO method will later be added in a class only accessable by the admin - for testing reasons for now its implemented here
    public static void addUser(User user) {
        String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        String query = "INSERT INTO users (name, email, password, role, state) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, user.getName());
            stmt.setString(2, user.getEmail());
            stmt.setString(3, hashedPassword);
            stmt.setString(4, user.getRole().name());
            stmt.setString(5, user.getState().name());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}