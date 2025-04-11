package backend.logic;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.User;
import database.DatabaseConnection;

public class ReimbursementService {
	private User user;
	private float reimbursementAmount;
	private float supermarketLimit=2.5f;
    private float restaurantLimit = 3.0f;
    
    public ReimbursementService() {
    	
    }
    
    public ReimbursementService(User user) {
    	this.user=user;
    }
    
    public float getReimbursementAmount() {
    	return this.reimbursementAmount;
    }
    
    public float getLimit(InvoiceCategory category) {
    	if(category== InvoiceCategory.RESTAURANT) {
    		return restaurantLimit;
    	}
    	else return supermarketLimit;
    }
    
    public void setReimbursementAmount(float amount) {
    	this.reimbursementAmount=amount;
    }
    public void setLimit(InvoiceCategory category, float amount) {
    	if(category== InvoiceCategory.RESTAURANT) {
    		this.restaurantLimit=amount;
    	}
    	else this.supermarketLimit=amount;
    }
    
    public boolean addReimbursement(Invoice invoice, float amount) {
    	String sql = "INSERT INTO reimbursements (invoice_id, approved_amount, processed_date) VALUES (?, ?, ?)";
    	
    	try (Connection conn = DatabaseConnection.connect();
    	    	//FileInputStream fis = new FileInputStream(invoice.getFile());
    	        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
    	    	//stmt.setBinaryStream(2, fis, (int) invoice.getFile().length());
    		
    			stmt.setInt(1, invoice.getId());
    			stmt.setFloat(2, amount);
    			stmt.setDate(3, Date.valueOf(invoice.getDate()));
    	       

    	        int affectedRows = stmt.executeUpdate(); // SQL ausführen
    	        if (affectedRows > 0) {
    	            ResultSet generatedKeys = stmt.getGeneratedKeys();
    	            if (generatedKeys.next()) {
    	                invoice.setId(generatedKeys.getInt(1)); // Neue ID setzen
    	            }
    	            return true; // Erfolg
    	        }
    	    } catch (SQLException e) {
    	        e.printStackTrace();
    	    }
    	    return false; // Falls etwas schiefgeht
    }
}