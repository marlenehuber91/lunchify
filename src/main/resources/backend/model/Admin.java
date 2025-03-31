package backend.model;
import java.util.List;
import java.util.Map;

public class Admin extends User {

    public Admin(String name, String email, String password, UserRole role, UserState state) {
        super(name, email, password, role, state);
    }

    public List<Invoice> viewAllReimbursements() {
        // Logik zur Anzeige aller Erstattungen
        return null;
    }

    public void updateReimbursementRates(Map<String, Float> newRate) {
        // Logik zur Aktualisierung der Erstattungssätze
    }

    public void manageUsers(String action, User user) {
        // Logik zur Verwaltung von Benutzern
    }

    public void exportReimbursements(String format) {
        // Logik zum Exportieren von Erstattungen
    }

    public List<Invoice> detectAnomalies() {
        // Logik zur Erkennung von Anomalien
        return null;
    }
}