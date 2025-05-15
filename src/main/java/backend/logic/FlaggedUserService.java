package backend.logic;

import backend.interfaces.ConnectionProvider;
import backend.model.FlaggedUser;
import backend.model.Invoice;
import backend.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FlaggedUserService {
    public static ConnectionProvider connectionProvider;
    public List<FlaggedUser> flaggedUsers;

    public FlaggedUserService () {
        this.flaggedUsers = new ArrayList<>();
    }

    public static void setConnectionProvider(ConnectionProvider provider) {
        connectionProvider = provider;
    }

    public List<FlaggedUser> getFlaggedUsers() {


        return flaggedUsers;
    }


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

}
