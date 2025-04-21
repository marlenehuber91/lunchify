package backend.configs;

import backend.logic.InvoiceService;
import backend.logic.ReimbursementService;
import database.DatabaseConnection;

public class AppContext {
    public static void initialize() {
        ReimbursementService.setConnectionProvider(DatabaseConnection::connect);
        InvoiceService.setConnectionProvider(DatabaseConnection::connect);
    }
}