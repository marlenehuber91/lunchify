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


public class InvoiceService {
	public static ConnectionProvider connectionProvider;
	private User user; // is marekd as unused even if its used -> ignore PMD!
	public List<Invoice> invoices;
	
	public static void setConnectionProvider(ConnectionProvider provider) {
	        connectionProvider = provider;
	}
	
	
	public InvoiceService () {
		this.invoices = new ArrayList<>();
	}
	
	public InvoiceService(User user) {
        this.user = user;
        if (connectionProvider != null) {
            this.invoices = getAllInvoices(user);
        } else {
            this.invoices = new ArrayList<>();
        }
    }
	
	public boolean invoiceDateAlreadyUsed (LocalDate date, User user) {
	return invoiceDateAlreadyUsed(date, user, -1);
   }
	
	public boolean invoiceDateAlreadyUsed(LocalDate date, User user, long excludeInvoiceId) { //created by AI
		for (Invoice invoice : invoices) {
	        if (invoice.getDate().equals(date)
	            && (excludeInvoiceId == -1 || invoice.getId() != excludeInvoiceId)) {
	            return true;
	        }
	    }
	    return false;
	}
	
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

	public static boolean isWorkday (LocalDate date) {
		DayOfWeek dayOfWeek = date.getDayOfWeek();
		return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
	}
	
	public boolean isValidFloat(String text) { //created by AI (ChatGPT)
		return text.matches("^\\d+(\\.\\d+)?$");
	}
	
	public boolean isAmountValid(String text) {
		return (text!=null && isValidFloat(text));	
	}
	
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
	
	public List<Invoice> getInvoices (){
		return this.invoices;
	}

	public static boolean addInvoice(Invoice invoice) {
		LocalDate ocrDate = OCR.getDate();
		Float ocrAmount = OCR.getAmount();
		InvoiceCategory ocrCategory = OCR.getCategory();

		//TODO REMOVE DEBUGGIN LINE
		System.out.println("OCR Date: " + OCR.getDate());
		System.out.println("OCR Amount: " + OCR.getAmount());
		System.out.println("OCR Category: " + OCR.getCategory());

		//check for permanent flagged users
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
			// Optional: trotzdem fortsetzen oder lieber abbrechen?
		}

		//check for differences with ocr data if not already flagged because of permanent flag
		if (!invoice.isFlagged()) { // Nur wenn noch nicht durch permanent_flag gesetzt
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
	                } else {
	                    System.out.println("Keine Datei vorhanden.");
	                }

	                return invoice;
	            }
	        }
	    } catch (SQLException | IOException e) {
	        e.printStackTrace();
	    }
	    return null;
	}
	
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

			if (!oldInvoice.isFlagged() && updated) {
				try (PreparedStatement stmt = conn.prepareStatement("UPDATE invoices SET flagged = true WHERE id = ?")) {
					stmt.setInt(1, oldInvoice.getId());
					stmt.executeUpdate();
				}

				try (PreparedStatement stmt = conn.prepareStatement(
						"UPDATE reimbursements SET state = ? WHERE invoice_id = ?")) {
					stmt.setString(1, "FLAGGED"); // oder ReimbursementState.IN_REVIEW.name()
					stmt.setInt(2, oldInvoice.getId());
					stmt.executeUpdate();
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
