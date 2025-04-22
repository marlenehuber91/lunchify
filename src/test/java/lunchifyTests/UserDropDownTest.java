package lunchifyTests;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import backend.logic.SessionManager;
import backend.logic.UserService;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@ExtendWith(MockitoExtension.class)
public class UserDropDownTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        UserService.setConnectionProvider(() -> mockConnection);

        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("state")).thenReturn("ACTIVE");
        when(mockResultSet.getString("password"))
                .thenReturn(BCrypt.hashpw("sarah123", BCrypt.gensalt()));
        when(mockResultSet.getString("role")).thenReturn("EMPLOYEE");
        when(mockResultSet.getString("name")).thenReturn("Sarah Maier");
        when(mockResultSet.getInt("id")).thenReturn(1);

        User user = UserService.authenticate("sarah.maier@lunch.at", "sarah123");
        SessionManager.setCurrentUser(user);
    }

    @Test
    public void testLogoutClearsCurrentUser() {
        assertNotNull(SessionManager.getCurrentUser(), "SessionManager sollte vor dem Logout einen Benutzer enthalten.");
        SessionManager.logout();
        assertNull(SessionManager.getCurrentUser(), "SessionManager sollte nach dem Logout keinen Benutzer mehr enthalten.");
    }
}