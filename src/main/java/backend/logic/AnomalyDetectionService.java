package backend.logic;

import backend.interfaces.ConnectionProvider;
import backend.model.Anomaly;
import backend.model.FlaggedUser;
import backend.model.Invoice;
import database.DatabaseConnection;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import static backend.logic.InvoiceService.getInvoiceById;

/**
 * Service class for managing and detecting anomalies in invoice data.
 * <p>
 * This class provides methods for extracting anomalies from the database,
 * inserting new anomalies, checking suspicious users, and removing flags.
 */

public class AnomalyDetectionService  {

    private static final String QUERY = """
            SELECT a.id, a.detected_at, i.id AS invoice_id, i.date, i.amount, i.category,
                   u.id AS user_id, u.name
            FROM AnomalyDetection a
            JOIN Invoices i ON a.invoice_id = i.id
            JOIN Users u ON a.user_id = u.id
            """;

/** secures DatabaseConnection */
    public static ConnectionProvider connectionProvider = new ConnectionProvider() {
        @Override
        public Connection getConnection() {
            return DatabaseConnection.connect();
        }
    };

    /** extracts anomalies from the database and returns them as a list
     * @return a list of Anomaly objects*/

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

    /** inserts a new anomaly into the database based on the given invoice
     * @param flaggedInvoice - the invoice that was flagged as suspicious
     */
    public static void detectAnomaliesAndLog(Invoice flaggedInvoice) {
        String sql = "INSERT INTO AnomalyDetection (invoice_id, user_id) VALUES (?, ?)";
        try (Connection conn = connectionProvider.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, flaggedInvoice.getId());
            stmt.setInt(2, flaggedInvoice.getUser().getId());

            int affectedRows = stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** detects if a user is flagged as suspicious and returns a FlaggedUser object
     * containing the user's ID and the number of flagged invoices.
     * @param userId - the ID of the user whose flagged invoices should be detected.
     * @return FlaggedUser - object of the user with the detected flagged invoices.
     * @throws SQLException
     */
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

    /** removes the flag from an invoice and updates the user's flagged invoices counter
     * @param anomaly - the anomaly that was handled
     * @throws SQLException
     */
    public void handleAnomalyDone(Anomaly anomaly) {
        try {
            Invoice invoice = getInvoiceById(anomaly.getInvoiceId());
            invoice.setFlag(false);
            removeFlag(invoice);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Removes the flag from an invoice and updates the user's flagged invoices counter.
     * @param invoice - the invoice that was found to no longer be suspicious.
     */
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
