package backend.logic;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import backend.interfaces.ConnectionProvider;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;

public class ReimbursementService {
	public static ConnectionProvider connectionProvider;
	private User user;
	private float reimbursementAmount;
	private float supermarketLimit = 2.5f;
	private float restaurantLimit = 3.0f;
	private float undetectableLimit = 2.5f;
	
	public static void setConnectionProvider(ConnectionProvider provider) {
        connectionProvider = provider;
    }
	
	public ReimbursementService() {
		if (connectionProvider != null) {
			loadLimitsFromDatabase();
		}
	}

	public ReimbursementService(User user) {
		this.user = user;
		if (connectionProvider != null) {
			loadLimitsFromDatabase();
		}
	}

	public float getReimbursementAmount() {
		return this.reimbursementAmount;
	}

	public float getLimit(InvoiceCategory category) {
		if (category == InvoiceCategory.RESTAURANT) {
			return restaurantLimit;
		} else if (category == InvoiceCategory.SUPERMARKET) {
			return supermarketLimit;
		} else {
			return undetectableLimit;
		}
	}

	public void setReimbursementAmount(float amount) {
		this.reimbursementAmount = amount;
	}

	public boolean addReimbursement(Invoice invoice, float amount) {
		if (connectionProvider == null) {
			throw new IllegalStateException("ConnectionProvider ist nicht gesetzt!");
		}
		
		String sql = "INSERT INTO reimbursements (invoice_id, approved_amount, processed_date) VALUES (?, ?, ?)";

		try (Connection conn = connectionProvider.getConnection();
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


	public boolean isValidFloat(String text) { // created by AI (ChatGPT)
		return text.matches("^\\d+(\\.\\d+)?$");
	}

	public boolean isAmountValid(String text) {
		return (text != null && isValidFloat(text));
	}

	public boolean modifyLimits(InvoiceCategory category, float newLimit) {
		if (newLimit < 0)
			throw new IllegalArgumentException("Limit should be a positive number.");
		else {
			try {
 				String sql = "UPDATE reimbursementAmount SET amount = ? WHERE category = ?::invoicecategory";
 				Connection conn = connectionProvider.getConnection();
 				PreparedStatement stmt = conn.prepareStatement(sql);
 				stmt.setFloat(1, newLimit);
 				stmt.setString(2, category.name());
 
 				int rowsUpdated = stmt.executeUpdate();
 
 				// Update cached value
				if (rowsUpdated > 0) {
					switch (category) {
						case SUPERMARKET:
							supermarketLimit = newLimit;
							break;
						case RESTAURANT:
							restaurantLimit = newLimit;
							break;
						case UNDETECTABLE:
							undetectableLimit = newLimit;
							break;
						default:
							break;
					}
					setReimbursementAmount(newLimit);
					return true;
				}
 				return false;
 			} catch (SQLException e) {
 				e.printStackTrace();
 			}
		}
		return false;
	}

	//created with help from AI
	private void loadLimitsFromDatabase() {
 		try {
 			Connection conn = connectionProvider.getConnection();
 
 			String query = "SELECT amount FROM reimbursementAmount WHERE category = ?::invoicecategory";
 			PreparedStatement stmt = conn.prepareStatement(query);
 
 			// Load supermarket limit
 			stmt.setString(1, InvoiceCategory.SUPERMARKET.name());
 			ResultSet rsSupermarket = stmt.executeQuery();
 			if (rsSupermarket.next()) {
 				supermarketLimit = rsSupermarket.getFloat("amount");
 			}
 
 			// Load restaurant limit
 			stmt.setString(1, InvoiceCategory.RESTAURANT.name());
 			ResultSet rsRestaurant = stmt.executeQuery();
 			if (rsRestaurant.next()) {
 				restaurantLimit = rsRestaurant.getFloat("amount");
 			}

			stmt.setString(1, InvoiceCategory.UNDETECTABLE.name());
			ResultSet rsUndetactable = stmt.executeQuery();
			if (rsUndetactable.next()) {
				undetectableLimit = rsUndetactable.getFloat("amount");
			}

 			rsSupermarket.close();
 			rsRestaurant.close();
			rsUndetactable.close();
 			stmt.close();
		} catch (SQLException e) {
 			e.printStackTrace();
 		}
	}

	public List<Reimbursement> getReimbursements(String condition, int userId) {
		List<Reimbursement> reimbursements = new ArrayList<>();

		String sql = "SELECT r.id AS reimbId, approved_amount, processed_date, date, r.status AS status, user_id,"
				+ "i.id AS invoice_id, i.amount AS invoiceAmount, i.category AS category, u.email AS userEmail " 
				+ "FROM Reimbursements r "
				+ "JOIN Invoices i ON r.invoice_id = i.id " 
				+ "JOIN Users u ON  i.user_id = u.id "
				+ "WHERE " + condition;

		try (Connection conn = connectionProvider.getConnection();
				 PreparedStatement stmt = conn.prepareStatement(sql))  {

			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				// Erstelle das Reimbursement-Objekt
				Reimbursement reimb = new Reimbursement();
				reimb.setId(rs.getInt("reimbId"));
				reimb.setApprovedAmount(rs.getFloat("approved_amount"));
				reimb.setProcessedDate(rs.getDate("processed_date"));
				reimb.setStatus(ReimbursementState.valueOf(rs.getString("status")));
				
				User user = new User();
				user.setId(rs.getInt("user_Id"));
				user.setEmail(rs.getString("userEmail"));

				// Erstelle das Invoice-Objekt und setze es
				Invoice invoice = new Invoice();
				invoice.setUser(user);
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

	public List<Reimbursement> getCurrentReimbursements(int userId) {
		return getReimbursements(
				"i.user_id = " + userId + " AND EXTRACT( MONTH FROM i.date) = EXTRACT(MONTH FROM CURRENT_DATE) "
						+ "AND EXTRACT(YEAR FROM i.date) = EXTRACT(YEAR FROM CURRENT_DATE)", userId);
	}

	public List<Reimbursement> getAllReimbursements(int userId) {
		return getReimbursements(("i.user_id = " + userId) ,userId );
	}
	
	public List<Reimbursement> getAllReimbursements(String condition) {
		return getReimbursements(("1 = 1"), 0);
	}

	public List<Reimbursement> getFilteredReimbursements(String selectedMonth, String selectedYear,
			String selectedCategory, String selectedStatus, int userId) {
		
		StringBuilder condition = new StringBuilder(buildUserFilterCondition(userId));
		// Monat filtern
		if (selectedMonth != null && !selectedMonth.isEmpty()) {
			condition.append(" AND EXTRACT(MONTH FROM i.date) = ").append(convertMonthToNumber(selectedMonth));
		}

		// Jahr filtern
		if (selectedYear != null && !selectedYear.isEmpty()) {
			condition.append(" AND EXTRACT(YEAR FROM i.date) = ").append(selectedYear);
		}

		// Kategorie filtern
		if (selectedCategory != null && !selectedCategory.isEmpty()) {
			condition.append(" AND i.category = '").append(selectedCategory).append("'");
		}

		// Status filtern
		if (selectedStatus != null && !selectedStatus.isEmpty()) {
			condition.append(" AND r.status = '").append(selectedStatus).append("'");
		}

		// Übergibt die Filter-Bedingungen an getReimbursements
		return getReimbursements(condition.toString(), userId);
	}

	public float getTotalReimbursement(List<Reimbursement> reimb) {
		float total = 0;
		for (Reimbursement reimbursement : reimb) {
			if (reimbursement.getStatus() != ReimbursementState.REJECTED)
				total += reimbursement.getApprovedAmount();
		}

		return total;
	}

	// Overload
	public float getTotalReimbursement(List<Reimbursement> reimb, ReimbursementState state) {
		float total = 0;
		for (Reimbursement reimbursement : reimb) {
			if (reimbursement.getStatus() == state)
				total += reimbursement.getApprovedAmount();
		}

		return total;
	}

	public String convertMonthToNumber(String month) {
		switch (month.toLowerCase()) {
		case "jänner":
			return "1";
		case "februar":
			return "2";
		case "märz":
			return "3";
		case "april":
			return "4";
		case "mai":
			return "5";
		case "juni":
			return "6";
		case "juli":
			return "7";
		case "august":
			return "8";
		case "september":
			return "9";
		case "oktober":
			return "10";
		case "november":
			return "11";
		case "dezember":
			return "12";
		case "alle":
			return null;
		default:
			throw new IllegalArgumentException("Ungültiger Monat: " + month);
		}
	}
	
	private String buildUserFilterCondition(int userId) {
		return userId > 0 ? "i.user_id = " + userId : "1=1";
	}

	public String getInfoText() {
		StringBuilder info = new StringBuilder();
		info.append("Pro Arbeitstag kann eine Rechnung eingereicht werden.").append(System.lineSeparator()).append(System.lineSeparator());
		info.append("Maximale Rückerstattung pro Arbeitstag:").append(System.lineSeparator()).append(System.lineSeparator());

		for (InvoiceCategory category : InvoiceCategory.values()) {
			if (category != InvoiceCategory.UNDETECTABLE) {
				float limit = getLimit(category);
				info.append(category.name().charAt(0))
						.append(category.name().substring(1).toLowerCase())
						.append(": ")
						.append(String.format("%.2f €", limit))
						.append(System.lineSeparator());
			}
		}

		return info.toString().trim();
	}
}