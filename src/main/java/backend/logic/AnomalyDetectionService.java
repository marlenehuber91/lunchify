package backend.logic;

import backend.interfaces.ConnectionProvider;
import backend.model.Anomaly;
import backend.model.Invoice;
import backend.model.User;
import database.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AnomalyDetectionService {

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
}
