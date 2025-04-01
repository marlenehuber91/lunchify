package backend.model;
import java.util.List;

import backend.logic.InvoiceService;


public class User {
    private int id; //created automatically by database
    private String name;
    private String email;
    private String password;
    private UserRole role;
    private UserState state;

    public User(String name, String email, String password, UserRole role, UserState state) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.state = state;
    }
    
    public String getName() {
    	return this.name;
    }

    public void login() {
        // Login-Logik hier
    }


    //TODO optional implementation if neccessary
    public void logout() {
        // Logout-Logik hier
    }

    public void uploadInvoice(Invoice invoice) {
        // Logik zum Hochladen einer Rechnung
    }

    public List<Invoice> viewCurrentReimbursement() {
        // Logik zur Anzeige aktueller Erstattungen
        return InvoiceService.getInvoices(this);
    }

    public List<Invoice> viewReimbursementHistory() {
        // Logik zur Anzeige der Erstattungsverlauf
        return null;
    }

    public void editInvoice(int invoiceId, Invoice newDetails) {
        // Logik zur Bearbeitung einer Rechnung
    }

    public void deleteInvoice(int invoiceId) {
        // Logik zum Löschen einer Rechnung
    }
}