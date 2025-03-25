package backend.logic;
import database.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;

//TODO not finished - still working on it


public class UserService {
    // Methode zur Benutzerüberprüfung
    public static boolean authenticate(String email, String password) {
        String query = "SELECT * FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Passwort-Hash aus der DB holen
                String storedPasswordHash = rs.getString("password_hash");
                // Passwort prüfen
                if (BCrypt.checkpw(password, storedPasswordHash)) {
                    String role = rs.getString("role");
                    System.out.println("Login erfolgreich! Rolle: " + role);
                    return true;
                }
            }
            return false; // Benutzer oder falsches Passwort
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Beispielmethode zum Hinzufügen eines Benutzers (nur einmalig beim Erstellen)
    public static void addUser(String email, String password, String role) {
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        String query = "INSERT INTO users (email, password_hash, role) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
