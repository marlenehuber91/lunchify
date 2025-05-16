package lunchifyTests;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import backend.model.Admin;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;

class AdminTests {
	
	private Admin admin;
    private static final String DEFAULT_NAME = "Default Admin";
    private static final String DEFAULT_EMAIL = "admin@example.com";
    private static final String DEFAULT_PASSWORD = "securePassword123";
    private static final UserRole DEFAULT_ROLE = UserRole.ADMIN;
    private static final UserState DEFAULT_STATE = UserState.ACTIVE;

    @BeforeEach
    void setUp() {
        // Wird vor JEDEM Test ausgeführt
        admin = new Admin(
            DEFAULT_NAME,
            DEFAULT_EMAIL,
            DEFAULT_PASSWORD,
            DEFAULT_ROLE,
            DEFAULT_STATE
        );
    }
    
    @Test
    void testAdminCreation() {
        assertNotNull(admin, "Admin-Objekt sollte nicht null sein");
        assertEquals(DEFAULT_NAME, admin.getName(), "Name sollte übereinstimmen");
        assertEquals(DEFAULT_EMAIL, admin.getEmail(), "E-Mail sollte übereinstimmen");
        assertEquals(DEFAULT_PASSWORD, admin.getPassword(), "Passwort sollte übereinstimmen");
        assertEquals(DEFAULT_ROLE, admin.getRole(), "Rolle sollte ADMIN sein");
        assertEquals(DEFAULT_STATE, admin.getState(), "Status sollte ACTIVE sein");
    }
    
    @Test
    void testAdminIsInstanceOfUser() {
        assertTrue(admin instanceof User, "Admin sollte eine Unterklasse von User sein");
    }
}
