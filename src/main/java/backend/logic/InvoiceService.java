package backend.logic;
import database.DatabaseConnection;
import org.mindrot.jbcrypt.BCrypt;

import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.InvoiceState;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;

import java.sql.*;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

//TODO not finished - still working on it


public class InvoiceService {
    

	public static List<Invoice> getInvoices (User user) {//needs to be changed as soon as the database is ready
	
	List<Invoice> dummyInvoices = new ArrayList<>();
	//TODO: Liste mit SELECT aus der Datenbank befüllen
	
    dummyInvoices.add(new Invoice(
        LocalDate.of(2025, 3, 31), // Datum
        199.99f, // Betrag
        InvoiceCategory.RESTAURANT, // Kategorie
        InvoiceState.PENDING, // Status
        null, // Datei
        user // Benutzer
    ));

    dummyInvoices.add(new Invoice(
        LocalDate.of(2025, 2, 10),
        75.50f,
        InvoiceCategory.SUPERMARKET,
        InvoiceState.PENDING,
        null,
        user
    ));

    dummyInvoices.add(new Invoice(
        LocalDate.of(2024, 1, 5),
        500.00f,
        InvoiceCategory.SUPERMARKET,
        InvoiceState.APPROVED,
        null,
        user
    ));

    dummyInvoices.add(new Invoice(
        LocalDate.of(2024, 12, 20),
        320.75f,
        InvoiceCategory.RESTAURANT,
        InvoiceState.PENDING,
        null,
        user
    ));
    
    return dummyInvoices;
    
	}
	
	public boolean invoiceDateAlreadyUsed (LocalDate date, User user) {
		List<Invoice> invoices = new ArrayList<>();
		invoices=getInvoices(user);
		
	   for (Invoice invoice: invoices) {
		   if (invoice.getDate().equals(date)) return true;
	   } 
	   return false;
   }
	
	public boolean isWorkday (LocalDate date) {
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
	}
	
	public boolean isValidFloat(String text) { //created by AI (ChatGPT)
		return text.matches("^\\d+(\\.\\d+)?$");
	}
	
	public boolean isamaountValid(String text) {
		return (text!=null && isValidFloat(text));	
	}
}
