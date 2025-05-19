package backend.logic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import backend.exceptions.InfrastructureException;
import backend.exceptions.ReimbursementException;
import backend.interfaces.ConnectionProvider;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import backend.model.UserRole;
import frontend.controller.ReimbursementHistoryController;

public class ReimbursementService extends ReimbursementHistoryController {
	public static ConnectionProvider connectionProvider;
	private User user; // is used but still marked as unused.. interesting - ignore in PMD!
	private float reimbursementAmount;
	private float supermarketLimit = 2.5f;
	private float restaurantLimit = 3.0f;
	private float undetectableLimit = 2.5f;

	boolean isAdmin;
	private User selectedUser;

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
			isAdmin = user.getRole().equals(UserRole.ADMIN);
		}
	}

	public User getSelectedUser() {
		return this.selectedUser;
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

	public void setSelectedUser(User selectedUser) {
		this.selectedUser = selectedUser;
	}
	@SuppressWarnings("PMD.AvoidThrowingRawExceptionTypes")
	public boolean addReimbursement(Invoice invoice, float amount) {
		if (connectionProvider == null) {
			throw new InfrastructureException("ConnectionProvider ist nicht gesetzt!");
		}

		String sql = "INSERT INTO reimbursements (invoice_id, approved_amount, processed_date, status) VALUES (?, ?, ?, ?)";


		try (Connection conn = connectionProvider.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setInt(1, invoice.getId());
			stmt.setFloat(2, amount);
			stmt.setDate(3, Date.valueOf(LocalDate.now()));
			if (invoice.isFlagged()) {
				stmt.setObject(4, ReimbursementState.FLAGGED, Types.OTHER); // PostgreSQL erfordert häufig Types.OTHER für benutzerdefinierte Enum-Typen
			} else {
				stmt.setObject(4, ReimbursementState.PENDING, Types.OTHER);
			}

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
		if (text == null) return false;
		else {
			return text.matches("^\\d+(\\.\\d+)?$");
		}
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
				reimb.setId(rs.getInt("reimbId"));

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
						+ "AND EXTRACT(YEAR FROM i.date) = EXTRACT(YEAR FROM CURRENT_DATE)",
				userId);
	}

	public List<Reimbursement> getAllReimbursements(int userId) {
		return getReimbursements(("i.user_id = " + userId), userId);
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
		info.append("Pro Arbeitstag kann eine Rechnung eingereicht werden.").append(System.lineSeparator())
				.append(System.lineSeparator());
		info.append("Maximale Rückerstattung pro Arbeitstag:").append(System.lineSeparator())
				.append(System.lineSeparator());

		for (InvoiceCategory category : InvoiceCategory.values()) {
			if (category != InvoiceCategory.UNDETECTABLE) {
				float limit = getLimit(category);
				info.append(category.name().charAt(0)).append(category.name().substring(1).toLowerCase()).append(": ")
						.append(String.format("%.2f €", limit)).append(System.lineSeparator());
			}
		}

		return info.toString().trim();
	}

	public boolean updateReimbursementIfChanged(Reimbursement oldReimb, Reimbursement newReimb, User reimbUser, boolean selfmade) {
		boolean updated = false;

		try (Connection conn = connectionProvider.getConnection()) {

			if (oldReimb.getApprovedAmount() != newReimb.getApprovedAmount()) {
				PreparedStatement stmt = conn
						.prepareStatement("UPDATE reimbursements SET approved_amount = ? WHERE id = ?");
				stmt.setFloat(1, newReimb.getApprovedAmount());
				stmt.setInt(2, oldReimb.getId());
				stmt.executeUpdate();
				updated = true;

				NotificationService.createNotification(reimbUser.getId(), "REIMBURSEMENT",
						oldReimb.getId(), "approved_amount", String.valueOf(oldReimb.getApprovedAmount()),
						String.valueOf(newReimb.getApprovedAmount()), "Der Erstattungsbetrag wurde geändert.",
						newReimb.getInvoice().getFile(), isAdmin, oldReimb.getInvoice().getDate(), selfmade);
			}

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return updated;
	}

	public boolean deleteReimbursement(Reimbursement toDeleteReimb, User reimbUser, boolean selfmade) {
		boolean deleted = false;

		try (Connection conn = connectionProvider.getConnection()) {
			int invoiceId = toDeleteReimb.getInvoice().getId();
			int reimbId = toDeleteReimb.getId();

			if (invoiceId != 0 && reimbId != 0) {
				PreparedStatement stmtReimb = conn.prepareStatement("DELETE FROM reimbursements WHERE id = ?");
				stmtReimb.setInt(1, reimbId);
				stmtReimb.executeUpdate();

				PreparedStatement stmtInvoice = conn.prepareStatement("DELETE FROM invoices WHERE id = ?");
				stmtInvoice.setInt(1, invoiceId);
				stmtInvoice.executeUpdate();

				deleted = true;

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		NotificationService.createNotification(toDeleteReimb.getInvoice().getUser().getId(), "REIMBURSEMENT",
				toDeleteReimb.getId(), "delete", null, null,
				"Die Rechnung wurde gelöscht.",null, isAdmin, toDeleteReimb.getInvoice().getDate(), selfmade);

		return deleted;
	}

	public boolean approveReimbursement(Reimbursement toApproveReimb, User reimbUser, boolean selfmade) {
		boolean approved = false;

		try (Connection conn = connectionProvider.getConnection()) {
			int reimbId = toApproveReimb.getId();

			if (reimbId != 0) {
				PreparedStatement stmtReimb = conn
						.prepareStatement("UPDATE reimbursements SET status = 'APPROVED' WHERE id = ?");
				stmtReimb.setInt(1, reimbId);
				stmtReimb.executeUpdate();
				
				int rowsUpdated = stmtReimb.executeUpdate();
				approved = rowsUpdated > 0;

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		NotificationService.createNotification(toApproveReimb.getInvoice().getUser().getId(), "REIMBURSEMENT",
				toApproveReimb.getId(), "approve", null, null, "Die Rechnung wurde genehmigt.",
				toApproveReimb.getInvoice().getFile(), isAdmin, toApproveReimb.getInvoice().getDate(), selfmade);

		return approved;
    }

	public boolean rejectReimbursement(Reimbursement toRejectReimb, User reimbUser, boolean selfmade) {
		boolean rejected = false;

		try (Connection conn = connectionProvider.getConnection()) {
			int reimbId = toRejectReimb.getId();

			if (reimbId != 0) {
				PreparedStatement stmtReimb = conn
						.prepareStatement("UPDATE reimbursements SET status = 'REJECTED' WHERE id = ?");
				stmtReimb.setInt(1, reimbId);
				stmtReimb.executeUpdate();

				rejected = true;

			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		NotificationService.createNotification(toRejectReimb.getInvoice().getUser().getId(), "REIMBURSEMENT",
				toRejectReimb.getId(), "approve", null, null, "Die Rechnung wurde abgelehnt.",
				toRejectReimb.getInvoice().getFile(), isAdmin, toRejectReimb.getInvoice().getDate(), selfmade);


		return rejected;
	}

	public static Reimbursement getReimbursementByInvoiceId(int invoiceId) {
		String query = "SELECT r.id AS reimbursement_id, r.approved_amount AS approvedAmount, r.processed_date AS processedDate, r.status," +
				"i.id AS invoice_id, i.date, i.amount, i.category, i.user_id, i.flagged, i.file " +
				"FROM Reimbursements r " +
				"JOIN Invoices i ON r.invoice_id = i.id " +
				"WHERE r.invoice_id = ?";

		Reimbursement reimbursement = null;

		try (Connection conn = connectionProvider.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, invoiceId); // Parameter setzen

			try (ResultSet resultSet = stmt.executeQuery()) {
				if (resultSet.next()) {
					int reimbursementId = resultSet.getInt("reimbursement_id");
					float approvedAmount = resultSet.getFloat("approvedAmount");
					Date processedDate = resultSet.getDate("processedDate");
					ReimbursementState state = ReimbursementState.valueOf(resultSet.getString("status"));

					//invoice needed for reimbursement object
					int id = resultSet.getInt("invoice_id");
					LocalDate invoiceDate = resultSet.getDate("date").toLocalDate();
					float amount = resultSet.getFloat("amount");
					InvoiceCategory category = InvoiceCategory.valueOf(resultSet.getString("category"));
					int userId = resultSet.getInt("user_id");
					boolean flag = resultSet.getBoolean("flagged");

					//start 100% AI generated
					//TODO -> ask Johanna how she´s retrieving the file /where
					InputStream fileStream = resultSet.getBinaryStream("file");
					File tempFile = null;
					if (fileStream != null) {
						try {
							// Create temp file with PNG extension
							tempFile = File.createTempFile("invoice_", ".png");
							tempFile.deleteOnExit();

							// Stream the data to file
							try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
								byte[] buffer = new byte[4096]; // Larger buffer size
								int bytesRead;
								while ((bytesRead = fileStream.read(buffer)) != -1) {
									outputStream.write(buffer, 0, bytesRead);
								}
							}
						} catch (IOException e) {
							e.printStackTrace();
							// Optionally continue without the file
						} //end 100% AI generated

						UserService userService = new UserService();
						User user = userService.getUserById(userId);

						Invoice invoice = new Invoice(invoiceDate, amount, category, tempFile, user);
						invoice.setId(id); //manually due to serial logic in database necessary
						if (flag) invoice.setFlag(true);

						reimbursement = new Reimbursement(invoice, approvedAmount, processedDate, state);
						reimbursement.setId(reimbursementId);
						reimbursement.setStatus(state);
					}
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}

			return reimbursement;
		} catch (SQLException e) {
			throw new ReimbursementException("Fehler beim Abrufen der Erstattung für invoiceId=" + invoiceId, e);

        }
    }

	public List<Reimbursement> getAllReimbursements(int month, int year) {
		List<Reimbursement> reimbursements = new ArrayList<>();
		String query = """
        SELECT r.id AS r_id, r.approved_amount, r.processed_date, r.status,
   			i.id AS invoice_id, i.user_id, u.id AS user_id, u.name AS user_name
         FROM reimbursements r
         JOIN invoices i ON r.invoice_id = i.id
         JOIN users u ON i.user_id = u.id
         WHERE r.status = 'APPROVED'
         AND EXTRACT(MONTH FROM r.processed_date) = ?\s
         AND EXTRACT(YEAR FROM r.processed_date) = ?                                                     
    """;

		try (Connection conn = connectionProvider.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, month);
			stmt.setInt(2, year);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					User user = new User();
					user.setId(rs.getInt("user_id"));
					user.setName(rs.getString("user_name"));

					Invoice invoice = new Invoice();
					invoice.setId(rs.getInt("invoice_id"));
					invoice.setUser(user);

					Reimbursement reimb = new Reimbursement();
					reimb.setId(rs.getInt("r_id"));
					reimb.setApprovedAmount(rs.getFloat("approved_amount"));
					reimb.setProcessedDate(rs.getDate("processed_date"));
					reimb.setStatus(ReimbursementState.valueOf(rs.getString("status")));
					reimb.setInvoice(invoice);

					reimbursements.add(reimb);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return reimbursements;
	}





}