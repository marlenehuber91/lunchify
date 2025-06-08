package backend.logic;

import backend.interfaces.ConnectionProvider;
import backend.model.FlaggedUser;
import database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * The FlaggedUserService class provides functionality to manage and query users who have been flagged
 * for suspicious activity or violations in the system. It handles operations such as retrieving flagged
 * users, adding or updating flag counts, and removing permanent flags.
 *
 * <p>This service interacts with the database to maintain and update flag information for users,
 * including the number of flags and whether a permanent flag has been set.</p>
 *
 * <p>Key features include:
 * <ul>
 *   <li>Retrieving a list of all flagged users with their flag counts</li>
 *   <li>Adding new flags or updating existing flag counts for users</li>
 *   <li>Removing permanent flags (with restrictions)</li>
 * </ul>
 * </p>
 */
public class FlaggedUserService {
    public List<FlaggedUser> flaggedUsers;

    /**
     * Constructs a new FlaggedUserService with an empty list of flagged users.
     */
    public FlaggedUserService() {
        this.flaggedUsers = new ArrayList<>();
    }

    /** secures DatabaseConnection */
    public static ConnectionProvider connectionProvider = new ConnectionProvider() {
        @Override
        public Connection getConnection() {
            return DatabaseConnection.connect();
        }
    };

    /**
     * Retrieves all flagged users from the database along with their flag information.
     *
     * @return a List of FlaggedUser objects containing user IDs, names, flag counts,
     *         and permanent flag status
     */
    public List<FlaggedUser> getFlaggedUsers() {
        List<FlaggedUser> flaggedUsers = new ArrayList<>();

        String selectSql = "SELECT f.user_id, u.name, f.no_flaggs, f.permanent_flag " +
                "FROM FlaggedUsers f JOIN Users u ON f.user_id = u.id";


        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(selectSql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int userId = rs.getInt("user_id");
                String userName = rs.getString("name");
                int noFlaggs = rs.getInt("no_flaggs");
                boolean permanentFlag = rs.getBoolean("permanent_flag");

                FlaggedUser user = new FlaggedUser(userId);
                user.setNoFlaggs(noFlaggs);
                user.setUserName(userName);
                user.setPermanentFlag(permanentFlag);
                flaggedUsers.add(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flaggedUsers;
    }

    /**
     * Adds a new flag for a user or updates the existing flag count if the user is already flagged.
     *
     * @param user the FlaggedUser object containing the user ID and flag information to add/update
     * @throws SQLException if a database access error occurs
     */
    public static void addOrUpdateFlaggedUser(FlaggedUser user) throws SQLException {
        String selectSql = "SELECT no_flaggs FROM FlaggedUsers WHERE user_id = ?";
        String updateSql = "UPDATE FlaggedUsers SET no_flaggs = ?, permanent_flag = ? WHERE user_id = ?";
        String insertSql = "INSERT INTO flaggedUsers (user_id, no_flaggs, permanent_flag) VALUES (?, ?, ?)";

        try (Connection conn = connectionProvider.getConnection()) {
            try (PreparedStatement selectStmt = conn.prepareStatement(selectSql)) {
                selectStmt.setInt(1, user.getUserId());
                ResultSet rs = selectStmt.executeQuery();

                if (rs.next()) {
                    int currentFlags = rs.getInt("no_flaggs");
                    int newFlags = currentFlags + user.getNoFlaggs();

                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setInt(1, newFlags);
                        updateStmt.setBoolean(2, user.isPermanentFlag());
                        updateStmt.setInt(3, user.getUserId());
                        updateStmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                        insertStmt.setInt(1, user.getUserId());
                        insertStmt.setInt(2, user.getNoFlaggs());
                        insertStmt.setBoolean(3, user.isPermanentFlag());
                        insertStmt.executeUpdate();
                    }
                }
            }
        }
    }

    /**
     * Removes a permanent flag from a user's record.
     *
     * @param userId the ID of the user to remove the permanent flag from
     * @throws SQLException if a database access error occurs
     * @throws IllegalArgumentException if attempting to remove a permanent flag from the current user
     */
    public void removePermanentFlag(int userId) throws SQLException {
        int currentUserId = SessionManager.getCurrentUser().getId();
        if (userId == currentUserId) {
            throw new IllegalArgumentException("Eigene Permanent Flag kann nicht entfernt werden.");
        }

        String sql = "UPDATE FlaggedUsers SET permanent_flag = false WHERE user_id = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }


}
