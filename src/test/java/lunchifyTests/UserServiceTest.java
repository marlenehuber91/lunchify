package lunchifyTests;

import backend.Exceptions.AuthenticationException;
import backend.logic.UserService;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mindrot.jbcrypt.BCrypt;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private Connection mockConnection;

    @Mock
    private PreparedStatement mockStatement;

    @Mock
    private ResultSet mockResultSet;

    @BeforeEach
    public void setUp() throws Exception {
        mockConnection = mock(Connection.class);
        mockStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        UserService.setConnectionProvider(() -> mockConnection);
    }

    @Test
    public void AuthenticateValidUser() throws Exception {
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("state")).thenReturn("ACTIVE");
        when(mockResultSet.getString("password"))
                .thenReturn(BCrypt.hashpw("sarah123", BCrypt.gensalt()));
        when(mockResultSet.getString("role")).thenReturn("EMPLOYEE");
        when(mockResultSet.getString("name")).thenReturn("Sarah Maier");
        when(mockResultSet.getInt("id")).thenReturn(1);

        User user = UserService.authenticate("sarah.maier@lunch.at", "sarah123");

        assertNotNull(user);
        assertEquals("Sarah Maier", user.getName());
        assertEquals(UserRole.EMPLOYEE, user.getRole());
        assertEquals(UserState.ACTIVE, user.getState());
    }

    @Test
    public void AuthenticateValidAdmin() throws Exception {
        when(mockResultSet.next()).thenReturn(true); // Ein Datensatz vorhanden
        when(mockResultSet.getString("state")).thenReturn("ACTIVE");
        when(mockResultSet.getString("password"))
                .thenReturn(BCrypt.hashpw("martin123", BCrypt.gensalt()));
        when(mockResultSet.getString("role")).thenReturn("ADMIN");
        when(mockResultSet.getString("name")).thenReturn("Martin Lechner");
        when(mockResultSet.getInt("id")).thenReturn(2);

        User user = UserService.authenticate("martin.lechner@lunch.at", "martin123");

        assertNotNull(user);
        assertEquals("Martin Lechner", user.getName());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertEquals(UserState.ACTIVE, user.getState());
    }

    @Test
    public void wrongPassword() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("state")).thenReturn("ACTIVE");
        when(mockResultSet.getString("password")).thenReturn(BCrypt.hashpw("correct_password", BCrypt.gensalt()));


        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                UserService.authenticate("martin.lechner@lunch.at", "wrong_password")
        );

        assertTrue(ex.getMessage().contains("Passwort ist nicht korrekt"));
    }

    @Test
    public void wrongEmail() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                UserService.authenticate("notfound@example.com", "irrelevant")
        );

        assertTrue(ex.getMessage().contains("E-Mail-Adresse wurde nicht gefunden"));
    }
}