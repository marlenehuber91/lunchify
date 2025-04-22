package lunchifyTests;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import frontend.controller.UserDropDownController;
import backend.logic.SessionManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class UserDropDownTest {

    @Mock
    private SessionManager mockSessionManager;

    private UserDropDownController controller;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Erstelle einen neuen UserDropDownController
        controller = new UserDropDownController();

        // Mock-SessionManager für die Controller-Logik setzen
        SessionManager.setInstance(mockSessionManager);
    }

    @Test
    public void verifyLogoutButtonVisibility() {
        // Initialisierung der UI-Logik simulieren
        controller.initialize();

        // Überprüfen, ob der MenuButton nicht null ist (sichtbar im Kontext)
        assertNotNull(controller.userDropDown, "Der 'Log Out'-Schaltflächen-Controller sollte initialisiert sein.");
    }

    @Test
    public void verifyLogoutFunctionality() throws Exception {
        // Mock Logout-Logik aufrufen
        doNothing().when(mockSessionManager).logout();

        // Logout-Aktion simulieren
        controller.handleLogout(null);

        // Verifiziere, dass SessionManager.logout() aufgerufen wurde
        verify(mockSessionManager, times(1)).logout();

        // Hier könntest du zusätzliche Assertions einfügen, falls du weitere Controller-Aktionen erwartest
        // z.B. verifizieren, dass die Login-Seite korrekt gesetzt wurde
    }
}