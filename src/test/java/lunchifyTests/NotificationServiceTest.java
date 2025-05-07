package lunchifyTests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import backend.interfaces.ConnectionProvider;
import backend.logic.NotificationService;
import backend.model.Notification;
import backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class NotificationServiceTest { //created by AI

    @Mock
    private ConnectionProvider connectionProvider;
    
    @Mock
    private Connection connection;
    
    @Mock
    private PreparedStatement preparedStatement;
    
    @Mock
    private ResultSet resultSet;
    
    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() throws SQLException {
        MockitoAnnotations.openMocks(this);
        when(connectionProvider.getConnection()).thenReturn(connection);
        NotificationService.setConnectionProvider(connectionProvider);
    }

    @Test
    void testCreateNotification() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        
        LocalDate testDate = LocalDate.now();
        notificationService.createNotification(1, "INVOICE", 123, "status", 
            "OLD", "NEW", "Message", null, false, testDate, false);
        
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setString(2, "INVOICE");
        verify(preparedStatement).setInt(3, 123);
        verify(preparedStatement).setString(4, "status");
        verify(preparedStatement).setString(5, "OLD");
        verify(preparedStatement).setString(6, "NEW");
        verify(preparedStatement).setString(7, "Message");
        verify(preparedStatement).setNull(8, Types.BINARY);
        verify(preparedStatement).setBoolean(9, false);
        verify(preparedStatement).setDate(10, Date.valueOf(testDate));
        verify(preparedStatement).setBoolean(11, false);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testGetNotificationsByUser() throws SQLException {
        User testUser = new User();
        testUser.setId(1);
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getLong("user_id")).thenReturn(1L);
        when(resultSet.getString("entity_type")).thenReturn("INVOICE");
        when(resultSet.getLong("entity_id")).thenReturn(123L);
        when(resultSet.getString("field_changed")).thenReturn("status");
        when(resultSet.getString("old_value")).thenReturn("OLD");
        when(resultSet.getString("new_value")).thenReturn("NEW");
        when(resultSet.getString("message")).thenReturn("Test message");
        when(resultSet.getBoolean("is_read")).thenReturn(false);
        when(resultSet.getTimestamp("created_at")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getBoolean("as_Admin")).thenReturn(false);
        when(resultSet.getDate("original_invoice_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(resultSet.getBoolean("is_selfmade_change")).thenReturn(false);
        
        List<Notification> result = notificationService.getNotificationsByUser(testUser);
        
        assertEquals(1, result.size());
        Notification notification = result.get(0);
        assertEquals(1L, notification.getId());
        assertEquals("INVOICE", notification.getEntityType());
        assertEquals("Test message", notification.getMessage());
    }

    @Test
    void testGetAdminNotification() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getBoolean("is_selfmade_change")).thenReturn(true);
        // Weitere Mock-Ergebnisse wie oben
        
        List<Notification> result = notificationService.getAdminNotification();
        
        assertEquals(1, result.size());
        assertTrue(result.get(0).getSelfmadeChange());
    }

    @Test
    void testMarkNotificationAsRead() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        
        notificationService.markNotificationAsRead(1L, true);
        
        verify(preparedStatement).setBoolean(1, true);
        verify(preparedStatement).setLong(2, 1L);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void testHasUnreadNotifications() {
        List<Notification> notifications = new ArrayList<>();
        Notification unread = new Notification();
        unread.setUserId(1L);
        unread.setRead(false);
        
        Notification read = new Notification();
        read.setUserId(1L);
        read.setRead(true);
        
        notifications.add(unread);
        notifications.add(read);
        
        assertTrue(notificationService.hasUnreadNotifications(notifications, 1L));
        
        notifications.remove(unread);
        assertFalse(notificationService.hasUnreadNotifications(notifications, 1L));
    }

    @Test
    void testGetNotificationWithUserId() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        // Weitere Mock-Ergebnisse
        
        List<Notification> result = notificationService.getNotification(false, 1);
        
        assertEquals(1, result.size());
        verify(preparedStatement).setBoolean(1, false);
        verify(preparedStatement).setLong(2, 1);
    }
    
    @Test
    void testGetNotificationWithoutUserId() throws SQLException {
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false);
        
        // Mock-Daten mit null-Wert für created_at
        when(resultSet.getLong("id")).thenReturn(1L);
        when(resultSet.getTimestamp("created_at")).thenReturn(null);
        when(resultSet.getDate("original_invoice_date")).thenReturn(Date.valueOf(LocalDate.now()));
        
        List<Notification> result = notificationService.getNotification(true, -1);
        
        assertEquals(1, result.size());
        assertNull(result.get(0).getCreatedAt()); // Überprüfen, ob null korrekt verarbeitet wurde
    }
}