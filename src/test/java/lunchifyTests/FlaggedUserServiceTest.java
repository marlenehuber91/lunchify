package lunchifyTests;

import backend.logic.SessionManager;
import backend.interfaces.ConnectionProvider;
import backend.logic.FlaggedUserService;
import backend.model.FlaggedUser;
import backend.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class FlaggedUserServiceTest {

    private FlaggedUserService flaggedUserService;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;
    private ConnectionProvider mockConnectionProvider;

    @BeforeEach
    void setUp() throws SQLException {
        // Mock the database objects
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
        mockConnectionProvider = mock(ConnectionProvider.class);

        // Set up the mock connection provider
        when(mockConnectionProvider.getConnection()).thenReturn(mockConnection);

        // Create the service with mocked connection provider
        flaggedUserService = new FlaggedUserService();
        flaggedUserService.connectionProvider = mockConnectionProvider;
    }

    @Test
    void testGetFlaggedUsers() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);

        // Mock result set behavior
        when(mockResultSet.next()).thenReturn(true, true, false); // two rows
        when(mockResultSet.getInt("user_id")).thenReturn(1, 2);
        when(mockResultSet.getString("name")).thenReturn("User1", "User2");
        when(mockResultSet.getInt("no_flaggs")).thenReturn(3, 5);
        when(mockResultSet.getBoolean("permanent_flag")).thenReturn(false, true);

        // Act
        List<FlaggedUser> result = flaggedUserService.getFlaggedUsers();

        // Assert
        assertEquals(2, result.size());

        FlaggedUser firstUser = result.get(0);
        assertEquals(1, firstUser.getUserId());
        assertEquals("User1", firstUser.getUserName());
        assertEquals(3, firstUser.getNoFlaggs());
        assertFalse(firstUser.isPermanentFlag());

        FlaggedUser secondUser = result.get(1);
        assertEquals(2, secondUser.getUserId());
        assertEquals("User2", secondUser.getUserName());
        assertEquals(5, secondUser.getNoFlaggs());
        assertTrue(secondUser.isPermanentFlag());
    }

    @Test
    void testGetFlaggedUsersWithSQLException() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("Database error"));

        // Act
        List<FlaggedUser> result = flaggedUserService.getFlaggedUsers();

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testAddOrUpdateFlaggedUser_InsertNew() throws SQLException {
        // Arrange
        FlaggedUser newUser = new FlaggedUser(1);
        newUser.setNoFlaggs(2);
        newUser.setPermanentFlag(false);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // No existing record

        // Act
        FlaggedUserService.addOrUpdateFlaggedUser(newUser);

        // Assert
        verify(mockPreparedStatement, times(1)).setInt(1, 1);
        verify(mockPreparedStatement, times(1)).setInt(2, 2);
        verify(mockPreparedStatement, times(1)).setBoolean(3, false);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testAddOrUpdateFlaggedUser_UpdateExisting() throws SQLException {
        // Arrange
        FlaggedUser existingUser = new FlaggedUser(1);
        existingUser.setNoFlaggs(3);
        existingUser.setPermanentFlag(true);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true); // Existing record
        when(mockResultSet.getInt("no_flaggs")).thenReturn(2); // Current flags

        // Act
        FlaggedUserService.addOrUpdateFlaggedUser(existingUser);

        // Assert
        verify(mockPreparedStatement, times(1)).setInt(1, 5); // 2 existing + 3 new
        verify(mockPreparedStatement, times(1)).setBoolean(2, true);
        verify(mockPreparedStatement, times(1)).setInt(3, 1);
        verify(mockPreparedStatement, times(1)).executeUpdate();
    }

    @Test
    void testRemovePermanentFlag() throws SQLException {
        // Arrange
        try (MockedStatic<SessionManager> mockedSessionManager = Mockito.mockStatic(SessionManager.class)) {
            User mockUser = mock(User.class);
            when(mockUser.getId()).thenReturn(2); // Different from userId we're testing
            mockedSessionManager.when(SessionManager::getCurrentUser).thenReturn(mockUser);

            when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

            // Act
            flaggedUserService.removePermanentFlag(1);

            // Assert
            verify(mockPreparedStatement, times(1)).setInt(1, 1);
            verify(mockPreparedStatement, times(1)).executeUpdate();
        }
    }

    @Test
    void testRemovePermanentFlag_SelfRemoval() {
        // Arrange
        try (MockedStatic<SessionManager> mockedSessionManager = Mockito.mockStatic(SessionManager.class)) {
            User mockUser = mock(User.class);
            when(mockUser.getId()).thenReturn(1); // Same as userId we're testing
            mockedSessionManager.when(SessionManager::getCurrentUser).thenReturn(mockUser);

            // Act & Assert
            assertThrows(IllegalArgumentException.class, () -> {
                flaggedUserService.removePermanentFlag(1);
            });
        }
    }
}