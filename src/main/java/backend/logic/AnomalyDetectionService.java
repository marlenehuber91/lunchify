package backend.logic;

import backend.interfaces.ConnectionProvider;
import backend.model.Anomaly;
import backend.model.FlaggedUser;
import backend.model.Invoice;

import database.DatabaseConnection;
import javafx.scene.control.Alert;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static backend.logic.InvoiceService.getInvoiceById;
import static java.sql.DriverManager.getConnection;

public class AnomalyDetectionService  {

    private static final String QUERY = """
            SELECT a.id, a.detected_at, i.id AS invoice_id, i.date, i.amount, i.category,
                   u.id AS user_id, u.name
            FROM AnomalyDetection a
            JOIN Invoices i ON a.invoice_id = i.id
            JOIN Users u ON a.user_id = u.id
            """;


    public static ConnectionProvider connectionProvider = new ConnectionProvider() {
        @Override
        public Connection getConnection() {
            return DatabaseConnection.connect();
        }
    };

    public List<Anomaly> extractAnomalies() {
        List<Anomaly> anomalies = new ArrayList<>();

        try (Connection connection = connectionProvider.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(QUERY)) {

            while (resultSet.next()) {
                int anomalyId = resultSet.getInt("id");
                Timestamp detectedAtTimestamp = resultSet.getTimestamp("detected_at");
                LocalDateTime detectedAt = detectedAtTimestamp != null ? detectedAtTimestamp.toLocalDateTime() : null;

                int invoiceId = resultSet.getInt("invoice_id");
                String invoiceDate = resultSet.getString("date");

                int userId = resultSet.getInt("user_id");
                String userName = resultSet.getString("name");

                anomalies.add(new Anomaly(anomalyId, detectedAt, invoiceId, userId, userName, invoiceDate));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return anomalies;
    }

    public static void detectAnomaliesAndLog(Invoice flaggedInvoice) {
        String sql = "INSERT INTO AnomalyDetection (invoice_id, user_id) VALUES (?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, flaggedInvoice.getId());
            stmt.setInt(2, flaggedInvoice.getUser().getId());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println("Anomalie erfolgreich gespeichert für Invoice ID: " + flaggedInvoice.getId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static FlaggedUser detectFlaggedUser(Integer userId) throws SQLException {
        String sql = "SELECT * FROM flaggedUsers WHERE user_id = ?";
        FlaggedUser flaggedUser = new FlaggedUser(userId);

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            ResultSet resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                int noFlaggs = resultSet.getInt("no_flaggs");
                boolean permanentFlag = resultSet.getBoolean("permanent_flag");

                flaggedUser.setNoFlaggs(noFlaggs);
                flaggedUser.setPermanentFlag(permanentFlag);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return flaggedUser;
    }


    public void handleAnomalyDone(Anomaly anomaly) {
        try {
            Invoice invoice = getInvoiceById(anomaly.getInvoiceId());
            invoice.setFlag(false);
            removeFlag(invoice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void removeFlag(Invoice invoice) {
        String sqlInvoice = "UPDATE invoices SET flagged = false WHERE id = ?";
        String sqlAnomaly = "DELETE FROM AnomalyDetection WHERE invoice_id = ?";

        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt1 = conn.prepareStatement(sqlInvoice);
             PreparedStatement stmt2 = conn.prepareStatement(sqlAnomaly)) {

            stmt1.setInt(1, invoice.getId());
            stmt1.executeUpdate();

            stmt2.setInt(1, invoice.getId());
            stmt2.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
