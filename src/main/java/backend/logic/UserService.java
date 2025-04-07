package backend.logic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import backend.Exceptions.AuthenticationException;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import database.DatabaseConnection;

public class UserService {

    public static User authenticate(String email, String password) throws AuthenticationException {
        String query = "SELECT id, name, password, role, state FROM users WHERE email = ?";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String state = rs.getString("state");
                if (state == null || state.isEmpty()) {
                    throw new AuthenticationException("Benutzerstatus fehlt in der Datenbank.");
                }

                UserState userState;
                try {
                    userState = UserState.valueOf(state.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new AuthenticationException("Ungültiger Benutzerstatus: " + state, e);
                }

                if (userState == UserState.SUSPENDED) {
                    throw new AuthenticationException("Ihr Konto wurde gesperrt.");
                }
                if (userState == UserState.INACTIVE) {
                    throw new AuthenticationException("Ihr Konto ist inaktiv.");
                }

                String storedPasswordHash = rs.getString("password");
                if (storedPasswordHash == null || storedPasswordHash.isEmpty()) {
                    throw new AuthenticationException("Passwort fehlt in der Datenbank.");
                }

                if (!BCrypt.checkpw(password, storedPasswordHash)) {
                    throw new AuthenticationException("Passwort ist nicht korrekt.");
                }

                String role = rs.getString("role");
                if (role == null || role.isEmpty()) {
                    throw new AuthenticationException("Benutzerrolle fehlt in der Datenbank.");
                }

                UserRole userRole;
                try {
                    userRole = UserRole.valueOf(role.toUpperCase());
                } catch (IllegalArgumentException e) {
                    throw new AuthenticationException("Ungültige Benutzerrolle: " + role, e);
                }

                int userId = rs.getInt("id");
                String name = rs.getString("name");

                User currentUser = new User(userId, name, email, userRole, userState);
                SessionManager.setCurrentUser(currentUser);

                return currentUser;

            } else {
                throw new AuthenticationException("E-Mail-Adresse wurde nicht gefunden.");
            }

        } catch (SQLException e) {
            throw new AuthenticationException("Datenbankfehler bei der Authentifizierung: " + e.getMessage(), e);
        }
    }
}