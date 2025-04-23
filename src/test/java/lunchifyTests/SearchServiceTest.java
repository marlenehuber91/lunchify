package lunchifyTests;

import backend.interfaces.ConnectionProvider;
import backend.logic.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SearchServiceTest {

    private ConnectionProvider mockProvider;
    private Connection mockConnection;
    private PreparedStatement mockStatement;
    private ResultSet mockResultSet;

    private SearchService searchService;

    @BeforeEach
    void setUp() throws Exception {
        mockProvider = mock(ConnectionProvider.class);
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockProvider.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        SearchService.setConnectionProvider(mockProvider);
        searchService = new SearchService();
    }

    @Test
    void searchUsersReturnsMatchingEmails() throws Exception {
        when(mockResultSet.next()).thenReturn(true, true, false);
        when(mockResultSet.getString("email"))
                .thenReturn("anna@lunch.at", "alex@lunch.at");

        List<String> result = searchService.searchUsers("a");

        assertEquals(2, result.size());
        assertTrue(result.contains("anna@lunch.at"));
        assertTrue(result.contains("alex@lunch.at"));
        verify(mockStatement).setString(1, "a%");
        verify(mockStatement).setString(2, "a%");
    }

    @Test
    void searchUsersReturnsEmptyList() throws Exception {
        when(mockResultSet.next()).thenReturn(false);

        List<String> result = searchService.searchUsers("xyz");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void searchUsersThrowsException() {
        SearchService.setConnectionProvider(null);

        assertThrows(IllegalStateException.class, () -> {
            new SearchService().searchUsers("anything");
        });
    }
}
