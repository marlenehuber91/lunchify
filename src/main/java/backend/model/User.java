package backend.model;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import backend.logic.InvoiceService;
import database.DatabaseConnection;


public class User {
    private int id;
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
    //never use to add new user to database! only for local user copy
    public User(int id, String name, String email, UserRole role, UserState state) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.state = state;
    }

    public int getId() {
        return id;
    }
    
    public int getId() {
    	return this.id;
    }


    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }


    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }

    public void uploadInvoice(Invoice invoice) {
        // Logik zum Hochladen einer Rechnung
    }

    public List<Invoice> viewCurrentReimbursement() {
        // Logik zur Anzeige aktueller Erstattungen
        return null;
    }

    public void editInvoice(int invoiceId, Invoice newDetails) {
        // Logik zur Bearbeitung einer Rechnung
    }

    public void deleteInvoice(int invoiceId) {
        // Logik zum Löschen einer Rechnung
    }
}