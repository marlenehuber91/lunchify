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

import java.sql.*;

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
        // Test-Hook instead of an actual DB connection
        UserService.setConnectionProvider(() -> mockConnection);
    }

    @Test
    public void AuthenticateValidUser() throws Exception {
        String email = "sarah.maier@lunch.at";
        String password = "sarah123";
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = UserService.authenticate(email, password);

        assertNotNull(user);
        assertEquals("Sarah Maier", user.getName());
        assertEquals(UserRole.EMPLOYEE, user.getRole());
        assertEquals(UserState.ACTIVE, user.getState());
    }

    @Test
    public void AuthenticateValidAdmin() throws Exception {
        String email = "martin.lechner@lunch.at";
        String password = "martin123";
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = UserService.authenticate(email, password);

        assertNotNull(user);
        assertEquals("Martin Lechner", user.getName());
        assertEquals(UserRole.ADMIN, user.getRole());
        assertEquals(UserState.ACTIVE, user.getState());
    }

    @Test
    public void wrongPassword() throws Exception {
        String email = "martin.lechner@lunch.at";
        String password = "wrongPassword";
        String hashed = BCrypt.hashpw("martin123", BCrypt.gensalt());

        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                UserService.authenticate(email, password)
        );

        assertTrue(ex.getMessage().contains("Passwort ist nicht korrekt"));
    }

    @Test
    public void wrongEmail() throws Exception {
        AuthenticationException ex = assertThrows(AuthenticationException.class, () ->
                UserService.authenticate("notfound@example.com", "irrelevant")
        );

        assertTrue(ex.getMessage().contains("E-Mail-Adresse wurde nicht gefunden"));
    }
}