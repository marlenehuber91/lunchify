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

public class NotificationService { 

	public static ConnectionProvider connectionProvider;

	public static void setConnectionProvider(ConnectionProvider provider) {
		connectionProvider = provider;
	}

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
	
	public static List<Notification> getNotificationsByUser(User user) {
		return getNotification(false, user.getId());
	}
	
	public static List<Notification> getAdminNotification () {
		return getNotification(true, -1);
	}
	
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
	
	public static boolean hasUnreadNotifications(List<Notification> notifications, long userId) {
		 return notifications.stream()
			        .anyMatch(notification -> notification.getUserId() == userId && !notification.isRead());
	}
	
	public static boolean hasUnreadAdminNotifications(List<Notification> notifications, long userId) {
		 return notifications.stream()
			        .anyMatch(notification -> !notification.isRead());
	}
}
