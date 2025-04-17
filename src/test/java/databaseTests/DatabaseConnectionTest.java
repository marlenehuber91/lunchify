package databaseTests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import database.DatabaseConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;


//created by AI
public class DatabaseConnectionTest {

    private static final String URL = "jdbc:postgresql://localhost:5432/Lunchify";
    private static final String USER = "postgres";
    private static final String PASSWORD = "pass";

    Connection mockConnection;

    @BeforeEach
    void setup() {
        mockConnection = mock(Connection.class);
    }

    @Test
    void testConnectSuccess() {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            mockedDriverManager.when(() -> DriverManager.getConnection(URL, USER, PASSWORD))
                    .thenReturn(mockConnection);

            Connection conn = DatabaseConnection.connect();
            assertNotNull(conn);
            mockedDriverManager.verify(() -> DriverManager.getConnection(URL, USER, PASSWORD));
        }
    }

    @Test
    void testConnectFailure() {
        try (MockedStatic<DriverManager> mockedDriverManager = mockStatic(DriverManager.class)) {
            SQLException sqlException = new SQLException("DB down");
            mockedDriverManager.when(() -> DriverManager.getConnection(URL, USER, PASSWORD))
                    .thenThrow(sqlException);

            Connection conn = DatabaseConnection.connect();
            assertNull(conn);
            mockedDriverManager.verify(() -> DriverManager.getConnection(URL, USER, PASSWORD));
        }
    }
}
