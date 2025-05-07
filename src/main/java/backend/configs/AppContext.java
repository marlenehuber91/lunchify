package backend.configs;

import backend.logic.InvoiceService;
import backend.logic.NotificationService;
import backend.logic.ReimbursementService;
import backend.logic.StatisticsService;
import backend.logic.UserService;
import database.DatabaseConnection;

public class AppContext {
    public static void initialize() {
        ReimbursementService.setConnectionProvider(DatabaseConnection::connect);
        InvoiceService.setConnectionProvider(DatabaseConnection::connect);
        UserService.setConnectionProvider(DatabaseConnection::connect);
        StatisticsService.setConnectionProvider(DatabaseConnection::connect);
        NotificationService.setConnectionProvider(DatabaseConnection::connect);
    }
}