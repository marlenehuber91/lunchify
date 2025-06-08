package backend.logic;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import backend.interfaces.ConnectionProvider;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.User;
import backend.model.UserRole;
import backend.model.*;

import static backend.logic.AnomalyDetectionService.*;
import static backend.logic.FlaggedUserService.addOrUpdateFlaggedUser;

/**
 * Service class for managing invoices associated with users.
 * Provides functionality to upload, validate, and insert invoices,
 * and to check invoice data against OCR results.
 */
public class InvoiceService {
	public static ConnectionProvider connectionProvider;
	private User user; // is marekd as unused even if its used -> ignore PMD!
	public List<Invoice> invoices;

	/** secures database connection */
	public static void setConnectionProvider(ConnectionProvider provider) {
	        connectionProvider = provider;
	}

	/**
	 * Default constructor that initializes an empty invoice list.
	 */
	public InvoiceService () {
		this.invoices = new ArrayList<>();
	}
	/**
	 * Constructor that initializes the service with a given user.
	 * Loads all invoices for the user if a connection provider is available.
	 *
	 * @param user the user for whom invoices are to be managed
	 */
	public InvoiceService(User user) {
        this.user = user;
        if (connectionProvider != null) {
            this.invoices = getAllInvoices(user);
        } else {
            this.invoices = new ArrayList<>();
        }
    }

	/**
	 * Checks whether an invoice already exists for the given date and user.
	 *
	 * @param date the date to check
	 * @param user the user whose invoices are checked
	 * @return true if an invoice with the same date already exists, false otherwise
	 */
	public boolean invoiceDateAlreadyUsed (LocalDate date, User user) {
	return invoiceDateAlreadyUsed(date, user, -1);
   }

	/**
	 * Checks whether an invoice already exists for the given date and user,
	 * optionally excluding a specific invoice ID.
	 *
	 * @param date the date to check
	 * @param user the user whose invoices are checked
	 * @param excludeInvoiceId the ID of an invoice to exclude from the check
	 * @return true if a duplicate date is found, false otherwise
	 */
	public boolean invoiceDateAlreadyUsed(LocalDate date, User user, long excludeInvoiceId) { //created by AI
		for (Invoice invoice : invoices) {
	        if (invoice.getDate().equals(date)
	            && (excludeInvoiceId == -1 || invoice.getId() != excludeInvoiceId)) {
	            return true;
	        }
	    }
	    return false;
	}

	/**
	 * Validates whether a date is valid for invoice creation.
	 * A valid date must be within the current month, not in the future, and a workday.
	 *
	 * @param date the date to validate
	 * @return true if the date is valid, false otherwise
	 */
	public boolean isValidDate(LocalDate date) {
		if(date == null) {
			return false;
		}
		  // Das heutige Datum
        LocalDate today = LocalDate.now();

        // Überprüfen, ob das Datum im gleichen Monat und Jahr wie heute ist
        boolean isSameMonth = date.getMonth() == today.getMonth() && date.getYear() == today.getYear();

        // Überprüfen, ob das Datum nicht in der Zukunft liegt
        boolean isNotInFuture = !date.isAfter(today);

        // Beide Bedingungen müssen erfüllt sein
        return isSameMonth && isNotInFuture && isWorkday(date);
    }

	/**
	 * Checks whether the given date falls on a workday (Monday to Friday).
	 *
	 * @param date the date to check
	 * @return true if the date is a workday, false otherwise
	 */
	public static boolean isWorkday (LocalDate date) {
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
	}

	/**
	 * Checks whether a given string is a valid float number.
	 *
	 * @param text the text to validate
	 * @return true if the text represents a float, false otherwise
	 */
	public boolean isValidFloat(String text) { //created by AI (ChatGPT)
		return text.matches("^\\d+(\\.\\d+)?$");
	}

	/**
	 * Validates whether a given string can be interpreted as a valid amount (float).
	 *
	 * @param text the string to validate
	 * @return true if the amount is valid, false otherwise
	 */
	public boolean isAmountValid(String text) {
		return (text!=null && isValidFloat(text));	
	}

	/**
	 * Retrieves all invoices for the given user from the database.
	 *
	 * @param user the user whose invoices are to be retrieved
	 * @return a list of invoices
	 */
	public static List<Invoice> getAllInvoices (User user) {
		List<Invoice> invoices = new ArrayList<>();
				
		String sql = "SELECT id, amount, category, date FROM invoices WHERE user_id = ?";
		try (Connection conn = connectionProvider.getConnection();
	             PreparedStatement stmt = conn.prepareStatement(sql)) {

	            stmt.setInt(1, user.getId());
	            ResultSet rs = stmt.executeQuery();

	            while (rs.next()) {
	                Invoice invoice = new Invoice();
	                invoice.setId(rs.getInt("id"));
	                invoice.setAmount(rs.getFloat("amount"));
	                invoice.setCategory(InvoiceCategory.valueOf(rs.getString("category")));
	                invoice.setDate(rs.getDate("date").toLocalDate());
	                invoices.add(invoice);
	            }

	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
		return invoices;
	}


	/**
	 * Returns the list of invoices currently managed by this service instance.
	 *
	 * @return the list of invoices
	 */
	public List<Invoice> getInvoices (){
		return this.invoices;
	}


	/**
	 * Adds a new invoice to the database and handles OCR mismatch flagging and user flagging logic.
	 * Also persists flagged user updates if necessary.
	 *
	 * @param invoice the invoice to be added
	 * @return true if the invoice was successfully added, false otherwise
	 */
	public static boolean addInvoice(Invoice invoice) {
		LocalDate ocrDate = OCR.getDate();
		Float ocrAmount = OCR.getAmount();
		InvoiceCategory ocrCategory = OCR.getCategory();

		String checkPermFlag = "SELECT permanent_flag FROM FlaggedUsers WHERE user_id = ?";

		try (Connection conn = connectionProvider.getConnection();
			 PreparedStatement permFlagStmt = conn.prepareStatement(checkPermFlag)) {

			permFlagStmt.setInt(1, invoice.getUser().getId());
			try (ResultSet rs = permFlagStmt.executeQuery()) {
				if (rs.next() && rs.getBoolean("permanent_flag")) {
					invoice.setFlag(true);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		//check for differences with ocr data if not already flagged because of permanent flag
		if (!invoice.isFlagged()) {
			if (ocrDate == null || invoice.getDate() == null || !ocrDate.equals(invoice.getDate()) ||
					invoice.getAmount() == 0.0f || Math.abs(ocrAmount - invoice.getAmount()) > 0.0001 ||
					ocrCategory == null || invoice.getCategory() == null || !ocrCategory.equals(invoice.getCategory())) {
				invoice.setFlag(true);
			}
		}

		String sql = "INSERT INTO invoices (date, amount, category, user_id, file, flagged) VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection conn = connectionProvider.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			stmt.setDate(1, Date.valueOf(invoice.getDate()));
			stmt.setFloat(2, invoice.getAmount());
			stmt.setObject(3, invoice.getCategory(), Types.OTHER);
			stmt.setInt(4, invoice.getUser().getId());
			stmt.setBoolean(6, invoice.isFlagged());

			if (invoice.getFile() != null) {
				try {
					stmt.setBinaryStream(5, new FileInputStream(invoice.getFile()), (int) invoice.getFile().length());
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
			} else {
				stmt.setNull(5, Types.BINARY);
			}

			int affectedRows = stmt.executeUpdate();
			if (affectedRows > 0) {
				ResultSet generatedKeys = stmt.getGeneratedKeys();
				if (generatedKeys.next()) {
					invoice.setId(generatedKeys.getInt(1));
				}

				if (invoice.isFlagged()) {
					detectAnomaliesAndLog(invoice);
					FlaggedUser flaggedUser = detectFlaggedUser(invoice.getUserId());
					flaggedUser.setNoFlaggs(flaggedUser.getNoFlaggs() + 1);
					if (!flaggedUser.isPermanentFlag() && flaggedUser.getNoFlaggs() > 9) {
						flaggedUser.setPermanentFlag(true);
					}
					addOrUpdateFlaggedUser(flaggedUser);
				}

				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}

		return false;
	}

	/**
	 * Loads the invoice associated with the given reimbursement.
	 *
	 * This method retrieves the invoice details from the database using the reimbursement's ID,
	 * and constructs an {@link Invoice} object including any binary file attached to it.
	 * The method also sets the user who created the invoice (only by ID).
	 *
	 * @param reimbursement The reimbursement containing the ID used to find the related invoice.
	 * @return The corresponding {@link Invoice} object, or {@code null} if no invoice is found.
	 */
	public Invoice loadInvoice(Reimbursement reimbursement) {
	    String sql = "SELECT i.* FROM Invoices i " +
	                 "JOIN Reimbursements r ON i.id = r.invoice_id " +
	                 "WHERE r.id = ?";

	    try (Connection conn = connectionProvider.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setInt(1, reimbursement.getId());

	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                int id = rs.getInt("id");
	                LocalDate date = rs.getDate("date").toLocalDate();
	                float amount = rs.getFloat("amount");
	                String category = rs.getString("category");
	                int userId = rs.getInt("user_id");
	                InputStream fileStream = rs.getBinaryStream("file");

	                Invoice invoice = new Invoice();
	                invoice.setId(id);
	                invoice.setDate(date);
	                invoice.setAmount(amount);
	                invoice.setCategory(InvoiceCategory.valueOf(category));

	                User user = new User();
	                user.setId(userId);
	                invoice.setUser(user);

	                if (fileStream != null) {
	                    // Temporäre Datei erzeugen
	                    File tempFile = File.createTempFile("invoice_", ".tmp");
	                    tempFile.deleteOnExit(); // Löscht sich automatisch beim Beenden

	                    try (FileOutputStream outputStream = new FileOutputStream(tempFile)) {
	                        byte[] buffer = new byte[1024];
	                        int bytesRead;
	                        while ((bytesRead = fileStream.read(buffer)) != -1) {
	                            outputStream.write(buffer, 0, bytesRead);
	                        }
	                    }

	                    invoice.setFile(tempFile);
	                }

	                return invoice;
	            }
	        }
	    } catch (SQLException | IOException e) {
	        e.printStackTrace();
	    }
	    return null;
	}

	/**
	 * Updates an existing invoice in the database if any of its attributes have changed.
	 *
	 * This method compares the old and new invoice data and updates the database accordingly.
	 * It also triggers a notification and flags the invoice if a self-made change is detected.
	 * If the invoice was previously not flagged and was changed by the user (selfmade = true),
	 * the invoice will be flagged and its associated reimbursement status will be set to "FLAGGED".
	 * Anomaly detection and flagged user tracking will also be triggered in that case.
	 *
	 * @param oldInvoice   The existing invoice before changes.
	 * @param newInvoice   The invoice containing new values to compare.
	 * @param invoiceUser  The user associated with the invoice.
	 * @param selfmade     Indicates whether the change was made by the user themselves.
	 * @return {@code true} if at least one attribute was changed and updated; otherwise {@code false}.
	 */
	public boolean updateInvoiceIfChanged(Invoice oldInvoice, Invoice newInvoice, User invoiceUser, boolean selfmade) {
		boolean updated = false;
		
		String fieldChanged = "Foto";
		String text = "Es wurde ein neues Foto hochgeladen";
		String oldVal = "";
		String newVal = "";
		boolean isAdmin = user.getRole().equals(UserRole.ADMIN);

		try (Connection conn = connectionProvider.getConnection()) {
			
			if (!oldInvoice.getDate().equals(newInvoice.getDate())) {
	            PreparedStatement stmt = conn.prepareStatement("UPDATE invoices SET date = ? WHERE id = ?");
	            stmt.setDate(1, Date.valueOf(newInvoice.getDate()));
	            stmt.setInt(2, oldInvoice.getId());
	            stmt.executeUpdate();
	            updated = true;
	            fieldChanged ="Rechnungsdatum";
	            text = "Das Rechnungsdatum wurde geändert";
	            oldVal = Date.valueOf(oldInvoice.getDate()).toString();
	            newVal = Date.valueOf(newInvoice.getDate()).toString();
	        }

	        if (oldInvoice.getAmount() != newInvoice.getAmount()) {
	            PreparedStatement stmt = conn.prepareStatement("UPDATE invoices SET amount = ? WHERE id = ?");
	            stmt.setFloat(1, newInvoice.getAmount());
	            stmt.setInt(2, oldInvoice.getId());
	            stmt.executeUpdate();
	            updated = true;
	            fieldChanged ="Rechnungsbetrag";
	            text = "Der Rechnungsbetrag wurde geändert";
	            oldVal = String.valueOf(oldInvoice.getAmount());
	            newVal = String.valueOf(newInvoice.getAmount());

	        }

	        if (oldInvoice.getCategory() != newInvoice.getCategory()) {
	            PreparedStatement stmt = conn.prepareStatement("UPDATE invoices SET category = ? WHERE id = ?");
	            stmt.setObject(1, newInvoice.getCategory(), Types.OTHER);
	            stmt.setInt(2, oldInvoice.getId());
	            stmt.executeUpdate();
	            updated = true;
	            fieldChanged ="Kategorie";
	            text = "Die Kategorie wurde geändert";
	            oldVal = oldInvoice.getCategory().toString();
	            newVal = newInvoice.getCategory().toString();
	        }

	        if (newInvoice.getFile() != null) {
	            try (PreparedStatement stmt = conn.prepareStatement("UPDATE invoices SET file = ? WHERE id = ?")) {
	                stmt.setBinaryStream(1, new FileInputStream(newInvoice.getFile()), (int) newInvoice.getFile().length());
	                stmt.setInt(2, oldInvoice.getId());
	                stmt.executeUpdate();
	                updated = true;

	            } catch (FileNotFoundException e) {
	                e.printStackTrace();
	            }
	        }

			if (!oldInvoice.isFlagged() && updated && selfmade) {
				try (PreparedStatement stmt = conn.prepareStatement("UPDATE invoices SET flagged = true WHERE id = ?")) {
					stmt.setInt(1, oldInvoice.getId());
					stmt.executeUpdate();
				}

				try (PreparedStatement stmt = conn.prepareStatement(
						"UPDATE reimbursements SET status = ? WHERE invoice_id = ?")) {
					stmt.setObject(1, "FLAGGED", java.sql.Types.OTHER);
					stmt.setInt(2, oldInvoice.getId());
					stmt.executeUpdate();
				}
				FlaggedUser flaggedUser = new FlaggedUser(invoiceUser.getId());
				try {
					FlaggedUserService.addOrUpdateFlaggedUser(flaggedUser);
					AnomalyDetectionService.detectAnomaliesAndLog(oldInvoice);
				} catch (SQLException e) {
					e.printStackTrace();
				}

			}


		} catch (SQLException e) {
	        e.printStackTrace();
	    }

			NotificationService.createNotification(
				    invoiceUser.getId(),
				    "INVOICE",
				    oldInvoice.getId(),
				    fieldChanged,
				    oldVal,
				    newVal,
				    text,
				    oldInvoice.getFile(),
				    isAdmin,
				    oldInvoice.getDate(),
				    selfmade
				);

	    return updated;
	}

	/**
	 * Retrieves an invoice from the database by its unique ID.
	 *
	 * The returned invoice includes basic attributes such as ID, date, amount, category, and flagged status.
	 * It does not include the associated user or file content.
	 *
	 * @param id The ID of the invoice to retrieve.
	 * @return The {@link Invoice} object if found; otherwise {@code null}.
	 */
	public static Invoice getInvoiceById(int id) {
		String sql = "SELECT id, date, amount, category, flagged FROM Invoices WHERE id = ?";

		try (Connection conn = connectionProvider.getConnection();
			 PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				Invoice invoice = new Invoice();
				invoice.setId(rs.getInt("id"));
				invoice.setDate(rs.getDate("date").toLocalDate());
				invoice.setAmount(rs.getFloat("amount"));
				invoice.setCategory(InvoiceCategory.valueOf(rs.getString("category")));
				invoice.setFlag(rs.getBoolean("flagged"));
				return invoice;
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return null;
	}

}
