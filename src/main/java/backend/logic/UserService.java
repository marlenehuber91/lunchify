package backend.logic;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import backend.interfaces.ConnectionProvider;
import org.mindrot.jbcrypt.BCrypt;
import backend.Exceptions.AuthenticationException;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;

public class UserService {

    private static ConnectionProvider connectionProvider;

    public static void setConnectionProvider(ConnectionProvider provider) {
        connectionProvider = provider;
    }

    public static User authenticate(String email, String password) throws AuthenticationException {
        if (connectionProvider == null) {
            throw new IllegalStateException("ConnectionProvider ist nicht gesetzt!");
        }

        String query = "SELECT id, name, password, role, state FROM users WHERE email = ?";
        try (Connection conn = connectionProvider.getConnection();
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
    public static List<User> getAllUsers() {
        if (connectionProvider == null) {
            throw new IllegalStateException("ConnectionProvider ist nicht gesetzt!");
        }

        List<User> users = new ArrayList<>();

        String sql = "SELECT id, name, email, password, role, state FROM users";

        try (Connection conn = connectionProvider.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(UserRole.valueOf(rs.getString("role")));
                user.setState(UserState.valueOf(rs.getString("state")));
                users.add(user);
            }

        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }
    //TODO: check, if Method can be deleted
    public User getUserById(int userId) {
        User user = new User();

        String sql = "SELECT id, name, email, password, role, state FROM users WHERE id = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                user.setId(userId);
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(UserRole.valueOf(rs.getString("role")));
                user.setState(UserState.valueOf(rs.getString("state")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }
    public void updateUser(User editedUser) {
        if (connectionProvider == null) {
            throw new IllegalStateException("ConnectionProvider ist nicht gesetzt!");
        }

        String sql = "UPDATE users SET name = ?, email = ?, password = ?, role = ?, state = ? WHERE id = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, editedUser.getName());
            stmt.setString(2, editedUser.getEmail());
            stmt.setString(3, editedUser.hashPassword());
            stmt.setObject(4, editedUser.getRole().name(), Types.OTHER);
            stmt.setObject(5, editedUser.getState().name(), Types.OTHER);
            stmt.setInt(6, editedUser.getId());

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertUser(User newUser) {
        if (connectionProvider == null) {
            throw new IllegalStateException("ConnectionProvider ist nicht gesetzt!");
        }

        String sql = "INSERT INTO users (name, email, password, role, state) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newUser.getName());
            stmt.setString(2, newUser.getEmail());
            stmt.setString(3, newUser.hashPassword());
            stmt.setObject(4, newUser.getRole().name(), Types.OTHER);
            stmt.setObject(5, newUser.getState().name(), Types.OTHER);

            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    
    public static List<String> getAllUsersEmail () {
    	List<User> allUsers = getAllUsers();
    	List <String> userEmail = new ArrayList<>();
    	for (User user : allUsers) {
    		userEmail.add(user.getEmail());
    	}
    	return userEmail;
    }
    
	public static int getUserIdByEmail(String email) {
		String sql = "SELECT id, name, email, password, role, state FROM users WHERE email = ?";

		try (Connection conn = connectionProvider.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, email);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				return rs.getInt("id");

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return -1;
    }

    public static User getUserByEmail(String email) {
        if (connectionProvider == null) {
            throw new IllegalStateException("ConnectionProvider ist nicht gesetzt!");
        }

        User user = null;
        String sql = "SELECT * FROM users WHERE email = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                user = new User();
                user.setId(rs.getInt("id"));
                user.setName(rs.getString("name"));
                user.setEmail(rs.getString("email"));
                user.setPassword(rs.getString("password"));
                user.setRole(UserRole.valueOf(rs.getString("role")));
                user.setState(UserState.valueOf(rs.getString("state")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return user;
    }
}