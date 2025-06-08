package backend.logic;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import backend.interfaces.ConnectionProvider;
import backend.model.Notification;
import backend.model.User;

/**
 * Service class for managing notifications within the system.
 *
 * Provides static methods to create, retrieve, and update notifications stored in a relational database.
 */
public class NotificationService { 

	public static ConnectionProvider connectionProvider;

	/** secures database connection */
	public static void setConnectionProvider(ConnectionProvider provider) {
		connectionProvider = provider;
	}

	/**
	 * Creates a new notification entry in the database.
	 *
	 * @param userId The ID of the user the notification is associated with.
	 * @param entityType The type of the entity the notification refers to.
	 * @param entityId The ID of the entity the notification refers to.
	 * @param fieldChanged The name of the field that was changed.
	 * @param oldValue The old value before the change.
	 * @param newValue The new value after the change.
	 * @param message A descriptive message of the change.
	 * @param file An optional file to store with the notification (nullable).
	 * @param asAdmin Whether the change was made in an administrative context.
	 * @param originalInvoiceDate The original invoice date associated with the entity (nullable).
	 * @param selfmade Whether the change was made by the user themselves.
	 */
	public static void createNotification(int userId, String entityType, int entityId, String fieldChanged,
			String oldValue, String newValue, String message, File file, boolean asAdmin,
			LocalDate originalInvoiceDate, boolean selfmade) {
		String sql = "INSERT INTO notifications (user_id, entity_type, entity_id, field_changed, old_value, new_value,"
				+ "message, old_file, as_Admin, original_invoice_date, is_selfmade_change) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = connectionProvider.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setInt(1, userId);
			stmt.setString(2, entityType);
			stmt.setInt(3, entityId);
			stmt.setString(4, fieldChanged);
			stmt.setString(5, oldValue);
			stmt.setString(6, newValue);
			stmt.setString(7, message);
			
			if (file != null) { 
	            try {
					stmt.setBinaryStream(8, new FileInputStream(file));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}
	        } else {
	            stmt.setNull(8, Types.BINARY);
			}

			stmt.setBoolean(9, asAdmin);
			if (originalInvoiceDate != null) {
			    stmt.setDate(10, Date.valueOf(originalInvoiceDate));
			} else {
			    stmt.setNull(10, java.sql.Types.DATE);
			}
			stmt.setBoolean(11, selfmade);
			stmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves all notifications for a given user that are not marked as admin-level.
	 *
	 * @param user The user whose notifications should be retrieved.
	 * @return A list of {@link Notification} objects for the user.
	 */
	public static List<Notification> getNotificationsByUser(User user) {
		return getNotification(false, user.getId());
	}

	/**
	 * Retrieves all notifications that were marked as administrative (not user-made).
	 *
	 * @return A list of admin-level {@link Notification} objects.
	 */
	public static List<Notification> getAdminNotification () {
		return getNotification(true, -1);
	}

	/**
	 * Retrieves notifications based on whether they were self-made or admin-created.
	 *
	 * @param isSelfMadeChange Indicates whether to retrieve user-made (true) or admin (false) notifications.
	 * @param userId The user ID to filter by (ignored if set to -1).
	 * @return A list of matching {@link Notification} objects.
	 */
	public static List<Notification> getNotification(boolean isSelfMadeChange, int userId) {
		List<Notification> notifications = new ArrayList<>();

	    String sql = "SELECT * FROM notifications WHERE is_selfmade_change = ?";
	    if (userId != -1) {
	        sql += " AND user_id = ?";
	    }
	    sql += " ORDER BY created_at DESC";

	    try (Connection conn = connectionProvider.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {

	        stmt.setBoolean(1, isSelfMadeChange);
	        if (userId != -1) {
	            stmt.setLong(2, userId);
	        }

	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                Notification notification = new Notification();
	                notification.setId(rs.getLong("id"));
	                notification.setUserId(rs.getLong("user_id"));
	                notification.setEntityType(rs.getString("entity_type"));
	                notification.setEntityId(rs.getLong("entity_id"));
	                notification.setFieldChanged(rs.getString("field_changed"));
	                notification.setOldValue(rs.getString("old_value"));
	                notification.setNewValue(rs.getString("new_value"));
	                notification.setMessage(rs.getString("message"));
	                notification.setRead(rs.getBoolean("is_read"));
	                notification.setSelfmadeChange(rs.getBoolean("is_selfmade_change"));

	                Timestamp createdAt = rs.getTimestamp("created_at");
	                notification.setCreatedAt(createdAt != null ? createdAt.toLocalDateTime() : null);
 
	                Date invoiceDate = rs.getDate("original_invoice_date");
	                notification.setOriginalInvoiceDate(invoiceDate != null ? invoiceDate.toLocalDate() : null);
	                
	                
	                notifications.add(notification);
	            }
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return notifications;
	}
	/**
	 * Marks a given notification as read or unread.
	 *
	 * @param id The ID of the notification.
	 * @param newVal {@code true} to mark as read, {@code false} to mark as unread.
	 */
	public static void markNotificationAsRead(long id, boolean newVal) {
		String sql = "UPDATE notifications SET is_read = ? WHERE id = ?";
	    try (Connection conn = connectionProvider.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql)) {
	        stmt.setBoolean(1, newVal);
	        stmt.setLong(2, id);
	        stmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	/**
	 * Checks whether the given list of notifications contains any unread notifications for a specific user.
	 *
	 * @param notifications The list of notifications to check.
	 * @param userId The user ID to filter for.
	 * @return {@code true} if there are unread notifications for the user; {@code false} otherwise.
	 */
	public static boolean hasUnreadNotifications(List<Notification> notifications, long userId) {
		 return notifications.stream()
			        .anyMatch(notification -> notification.getUserId() == userId && !notification.isRead());
	}

	/**
	 * Checks whether the given list of admin notifications contains any unread entries.
	 *
	 * @param notifications The list of admin notifications to check.
	 * @param userId The user ID (ignored).
	 * @return {@code true} if there are any unread admin notifications; {@code false} otherwise.
	 */
	public static boolean hasUnreadAdminNotifications(List<Notification> notifications, long userId) {
		 return notifications.stream()
			        .anyMatch(notification -> !notification.isRead());
	}
}
