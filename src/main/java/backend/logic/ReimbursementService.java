package backend.logic;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import database.DatabaseConnection;

public class ReimbursementService {
	private User user;
	private float reimbursementAmount;
	private float supermarketLimit = 2.5f;
	private float restaurantLimit = 3.0f;

	public ReimbursementService() {

	}

	public ReimbursementService(User user) {
		this.user = user;
	}

	public float getReimbursementAmount() {
		return this.reimbursementAmount;
	}

	public float getLimit(InvoiceCategory category) {
		if (category == InvoiceCategory.RESTAURANT) {
			return restaurantLimit;
		} else
			return supermarketLimit;
	}

	public void setReimbursementAmount(float amount) {
		this.reimbursementAmount = amount;
	}
	
	 public boolean addReimbursement(Invoice invoice, float amount) {
	    	String sql = "INSERT INTO reimbursements (invoice_id, approved_amount, processed_date) VALUES (?, ?, ?)";
	    	
	    	try (Connection conn = DatabaseConnection.connect();
	    	        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
	    		
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

	public void setLimit(InvoiceCategory category, float amount) {
		if (category == InvoiceCategory.RESTAURANT) {
			this.restaurantLimit = amount;
		} else
			this.supermarketLimit = amount;
	}

	public boolean isValidFloat(String text) { // created by AI (ChatGPT)
		return text.matches("^\\d+(\\.\\d+)?$");
	}

	public boolean isAmountValid(String text) {
		return (text != null && isValidFloat(text));
	}

	public boolean modifyLimits(InvoiceCategory category, float newLimit) {
		if (newLimit < 0)
			throw new IllegalArgumentException("Limits dürfen nicht negativ sein.");
		else {
			if (category == InvoiceCategory.RESTAURANT) {
				restaurantLimit = newLimit;
			} else {
				supermarketLimit = newLimit;
			}
			return true;
		}
	}

	public List<Reimbursement> getReimbursements(String condition) {
		List<Reimbursement> reimbursements = new ArrayList<>();

		String sql = "SELECT r.id AS reimbId, approved_amount, processed_date, date, r.status AS status, user_id, "
				+ "i.id AS invoice_id, i.amount AS invoiceAmount, i.category AS category " + "FROM Reimbursements r "
				+ "JOIN Invoices i ON r.invoice_id = i.id " + "WHERE " + condition;

		try (Connection conn = DatabaseConnection.connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
			// Setze die Parameter für die Abfrage
			stmt.setInt(1, user.getId());

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				// Erstelle das Reimbursement-Objekt
				Reimbursement reimb = new Reimbursement();
				reimb.setId(rs.getInt("reimbId"));
				reimb.setApprovedAmount(rs.getFloat("approved_amount"));
				reimb.setProcessedDate(rs.getDate("processed_date"));
				reimb.setStatus(ReimbursementState.valueOf(rs.getString("status")));

				// Erstelle das Invoice-Objekt und setze es
				Invoice invoice = new Invoice();
				invoice.setId(rs.getInt("invoice_id"));
				invoice.setAmount(rs.getFloat("invoiceAmount"));
				invoice.setCategory(InvoiceCategory.valueOf(rs.getString("category")));
				invoice.setDate(rs.getDate("date").toLocalDate());
				reimb.setInvoice(invoice);

				reimbursements.add(reimb);

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reimbursements;
	}

	public List<Reimbursement> getCurrentReimbursements() {
		return getReimbursements("i.user_id = ? " 
				+ "AND EXTRACT( MONTH FROM i.date) = EXTRACT(MONTH FROM CURRENT_DATE) "
				+ "AND EXTRACT(YEAR FROM i.date) = EXTRACT(YEAR FROM CURRENT_DATE)");
	}

	public List<Reimbursement> getAllReimbursements() {
		return getReimbursements("i.user_id = ?;");
	}

	public int getUserId() {
		return this.user.getId();
	}
	
	public float getTotalReimbursement(List<Reimbursement> reimb) { 
		float total = 0;
		for (Reimbursement reimbursement: reimb) {
			total += reimbursement.getApprovedAmount();
		}
		
		return total;
		}
}