package lunchifyTests;


import backend.logic.ExportService;
import backend.logic.NotificationService;
import backend.logic.ReimbursementService;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import backend.interfaces.ConnectionProvider;
import frontend.controller.ReimbursementHistoryController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) 
class ReimbursementServiceTest { //all testcases are created with help of AI

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockStatement;
    @Mock private ResultSet mockResultSet;
    @Mock private ReimbursementService mockReimbursementService;

    private ReimbursementService service;
    private User testUser;

    @BeforeEach
    void setUp() throws Exception {
        testUser = new User(1, "dummy", "dummy@lunch.at", UserRole.ADMIN, UserState.ACTIVE);

        ConnectionProvider provider = () -> mockConnection;
        ReimbursementService.setConnectionProvider(provider);

        lenient().when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        lenient().when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS))).thenReturn(mockStatement);
        lenient().when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        service = new ReimbursementService(testUser);
        mockReimbursementService = new ReimbursementService(testUser);
        NotificationService.setConnectionProvider(provider);
    }

    @Test
    void testAddReimbursementSuccess() throws Exception {
        Invoice inv = new Invoice();
        inv.setId(10);
        inv.setDate(LocalDate.now());

        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(42);

        boolean result = service.addReimbursement(inv, 5.0f);

        assertTrue(result);
        assertEquals(42, inv.getId());
    }

    @Test
    void testGetTotalReimbursementDefault() {
        Invoice dummyInv = new Invoice();

        Reimbursement r1 = new Reimbursement(dummyInv, 3.0f, Date.valueOf(LocalDate.now()), ReimbursementState.APPROVED); // angepasst
        Reimbursement r2 = new Reimbursement(dummyInv, 2.0f, Date.valueOf(LocalDate.now()), ReimbursementState.REJECTED); // angepasst

        float total = service.getTotalReimbursement(Arrays.asList(r1, r2));
        assertEquals(3.0f, total);
    }

    @Test
    void testConvertMonthToNumberValid() {
        assertEquals("4", service.convertMonthToNumber("April"));
        assertEquals("12", service.convertMonthToNumber("Dezember"));
    }

    @Test
    void testConvertMonthToNumberInvalid() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.convertMonthToNumber("invalidMonth");
        });
        assertTrue(exception.getMessage().contains("Ungültiger Monat"));
    }

    @Test
    void testGetFilteredReimbursementsWithVariousFilters() {
        List<Reimbursement> result = service.getFilteredReimbursements("April", "2023", "RESTAURANT", "APPROVED", testUser.getId());
        assertNotNull(result);
    }

    @Test
    void testGetAllReimbursements() {
        List<Reimbursement> result = service.getAllReimbursements(testUser.getId());
        assertNotNull(result);
    }

    @Test
    void testGetCurrentReimbursements() {
        List<Reimbursement> result = service.getCurrentReimbursements(testUser.getId());
        assertNotNull(result);
    }

    @Test
    void testGetTotalReimbursementEmptyList() {
        float total = service.getTotalReimbursement(List.of());
        assertEquals(0.0f, total);
    }

    @Test
    void testGetTotalReimbursementAllRejected() {
        Invoice dummyInv = new Invoice();
        Reimbursement r1 = new Reimbursement(dummyInv, 1.0f, Date.valueOf(LocalDate.now()), ReimbursementState.REJECTED); // angepasst

        float total = service.getTotalReimbursement(List.of(r1));
        assertEquals(0.0f, total);
    }

    @Test
    void testGetTotalReimbursementByStateEmptyList() {
        float total = service.getTotalReimbursement(List.of(), ReimbursementState.APPROVED);
        assertEquals(0.0f, total);
    }

    @Test
    void testGetTotalReimbursementByStateNoMatch() {
        Invoice dummyInv = new Invoice();
        Reimbursement r1 = new Reimbursement(dummyInv, 2.0f, Date.valueOf(LocalDate.now()), ReimbursementState.REJECTED); // angepasst

        float total = service.getTotalReimbursement(List.of(r1), ReimbursementState.APPROVED);
        assertEquals(0.0f, total);
    }

    @Test
    void testGetFilteredReimbursementsWithAllMonth() {
        List<Reimbursement> result = service.getFilteredReimbursements("alle", "2023", "RESTAURANT", "APPROVED", testUser.getId());
        assertNotNull(result);
    }

    @Test //created by AI
    void testAddReimbursementConnectionProviderNullShouldThrowException() {
        // Arrange
        ReimbursementService.setConnectionProvider(null); // bewusst null setzen
        ReimbursementService service = new ReimbursementService();
        Invoice dummyInvoice = new Invoice(); // dummy Objekt, Details egal

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> {
            service.addReimbursement(dummyInvoice, 10.0f);
        }, "Expected IllegalStateException when connectionProvider is null");
    }

    @Test
    void testNewReimbursementHasPendingStatus() throws Exception {
        // Setup Invoice
        Invoice invoice = new Invoice();
        invoice.setId(1);
        invoice.setAmount(10.0f);
        invoice.setCategory(InvoiceCategory.RESTAURANT);
        invoice.setDate(LocalDate.now());

        // Mock INSERT
        when(mockConnection.prepareStatement(anyString(), anyInt())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        ResultSet mockKeys = mock(ResultSet.class);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockKeys);
        when(mockKeys.next()).thenReturn(true);
        when(mockKeys.getInt(1)).thenReturn(123);

        boolean added = service.addReimbursement(invoice, 3.0f);

        assertTrue(added);
        assertEquals(123, invoice.getId());

        // Jetzt simulieren wir eine Rückgabe mit Status PENDING
        PreparedStatement mockSelectStmt = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(startsWith("SELECT"))).thenReturn(mockSelectStmt);
        when(mockSelectStmt.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false); // nur ein Eintrag
        when(mockResultSet.getInt("reimbId")).thenReturn(123);
        when(mockResultSet.getFloat("approved_amount")).thenReturn(3.0f);
        when(mockResultSet.getDate("processed_date")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getString("status")).thenReturn("PENDING");

        when(mockResultSet.getInt("invoice_id")).thenReturn(10);
        when(mockResultSet.getInt("user_Id")).thenReturn(testUser.getId());
        when(mockResultSet.getFloat("invoiceAmount")).thenReturn(10.0f);
        when(mockResultSet.getString("category")).thenReturn("RESTAURANT");
        when(mockResultSet.getString("userEmail")).thenReturn("user@example.com");
        when(mockResultSet.getDate("date")).thenReturn(Date.valueOf(LocalDate.now()));

        List<Reimbursement> reimbursements = service.getAllReimbursements(testUser.getId());

        assertEquals(1, reimbursements.size());
        assertEquals(ReimbursementState.PENDING, reimbursements.get(0).getStatus());
    }

    @Test //created by AI - changed by the team
    void testGetFilteredReimbursementsReturnsCorrectItems() throws SQLException {
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("reimbId")).thenReturn(1);
        when(mockResultSet.getFloat("approved_amount")).thenReturn(5.0f);
        when(mockResultSet.getDate("processed_date")).thenReturn(Date.valueOf("2023-04-10"));
        when(mockResultSet.getString("status")).thenReturn("APPROVED");
        when(mockResultSet.getInt("user_Id")).thenReturn(1);
        when(mockResultSet.getInt("invoice_id")).thenReturn(10);
        when(mockResultSet.getFloat("invoiceAmount")).thenReturn(10.0f);
        when(mockResultSet.getString("category")).thenReturn("RESTAURANT");
        when(mockResultSet.getString("userEmail")).thenReturn("user@example.com");
        when(mockResultSet.getDate("date")).thenReturn(Date.valueOf("2023-04-01"));

        List<Reimbursement> filtered = service.getFilteredReimbursements("April", "2023", "RESTAURANT", "APPROVED", testUser.getId());

        assertEquals(1, filtered.size());
        assertEquals("RESTAURANT", filtered.get(0).getInvoice().getCategory().toString());
        assertEquals(ReimbursementState.APPROVED, filtered.get(0).getStatus());
    }
    
    @Test
    void testModifyLimitsSuccess() throws Exception {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = service.modifyLimits(InvoiceCategory.RESTAURANT, 4.5f);
        assertTrue(result);
        assertEquals(4.5f, service.getLimit(InvoiceCategory.RESTAURANT));
    }
    
    @Test
    void testModifyLimitsWithNegativeValueThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            service.modifyLimits(InvoiceCategory.SUPERMARKET, -1.0f);
        });
    }
    
    @Test
    void testIsValidFloatWithValidInput() {
        assertTrue(service.isValidFloat("10.5"));
        assertTrue(service.isValidFloat("7"));
    }
    
    @Test
    void testIsValidFloatWithInvalidInput() {
        assertFalse(service.isValidFloat("abc"));
        assertFalse(service.isValidFloat(""));
        assertFalse(service.isValidFloat(null));
    }
    
    @Test
    void testIsAmountValid() {
        assertTrue(service.isAmountValid("12.34"));
        assertFalse(service.isAmountValid("wrong"));
        assertFalse(service.isAmountValid(null));
    }
    
    @Test
    void testGetLimitForAllCategories() {
        assertEquals(3.0f, service.getLimit(InvoiceCategory.RESTAURANT));
        assertEquals(2.5f, service.getLimit(InvoiceCategory.SUPERMARKET));
        assertEquals(2.5f, service.getLimit(InvoiceCategory.UNDETECTABLE));
    }
    
    @Test
    void testGetReimbursementsReturnsEmptyList() throws Exception {
        when(mockResultSet.next()).thenReturn(false); // kein Ergebnis

        List<Reimbursement> result = service.getAllReimbursements(testUser.getId());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    
    @Test
    void testSetAndGetSelectedUser() {
        User otherUser = new User(2, "Anna", "anna@example.com", UserRole.EMPLOYEE, UserState.ACTIVE);
        service.setSelectedUser(otherUser);

        assertEquals(otherUser, service.getSelectedUser());
    }
    
    @Test
    void testGetReimbursementAmount() {
        ReimbursementService service = new ReimbursementService();
        service.setReimbursementAmount(42.0f);
        assertEquals(42.0f, service.getReimbursementAmount());
    }
    
    @Test
    void testGetInfoText_containsLimits() {
        ReimbursementService service = new ReimbursementService();
        service.setReimbursementAmount(3.0f); // Optional
        
        String text = service.getInfoText();
        
        assertTrue(text.contains("Pro Arbeitstag kann eine Rechnung eingereicht werden"));
        assertTrue(text.contains("Supermarket:") || text.contains("Supermarkt:"));
        assertTrue(text.contains("Restaurant:"));
    }
    //failing
    @Test
    void testApproveReimbursementFailure() throws Exception {
        Reimbursement testReimb = new Reimbursement();
        testReimb.setId(1);
        Invoice testInvoice = new Invoice();
        testInvoice.setId(1);
        testInvoice.setUser(testUser);
        testReimb.setInvoice(testInvoice);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(0); // No rows updated

        boolean result = service.approveReimbursement(testReimb, testUser, false);
        
        assertFalse(result);
    }
   
    @Test
    void testApproveReimbursementSuccess() throws Exception {
        Reimbursement testReimb = new Reimbursement();
        testReimb.setId(1);
        Invoice testInvoice = new Invoice();
        testInvoice.setId(1);
        testInvoice.setUser(testUser);
        testReimb.setInvoice(testInvoice);

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        boolean result = service.approveReimbursement(testReimb, testUser, false);
        
        assertTrue(result);
     }
    

	@Test
	void testApproveReimbursementWithNullReimbursementThrowsException() {
	    assertThrows(NullPointerException.class, () -> {
	        service.approveReimbursement(null, testUser, false);
	    });
	}
	
	@Test
	void testDeleteReimbursementSuccess() throws Exception {
	    Reimbursement testReimb = new Reimbursement();
	    testReimb.setId(1);
	    Invoice testInvoice = new Invoice();
	    testInvoice.setId(1);
	    testInvoice.setUser(testUser);
	    testReimb.setInvoice(testInvoice);

	    // Mock die beiden DELETE-Statements
	    when(mockConnection.prepareStatement(startsWith("DELETE FROM reimbursements")))
	        .thenReturn(mockStatement);
	    when(mockConnection.prepareStatement(startsWith("DELETE FROM invoices")))
	        .thenReturn(mockStatement);
	    when(mockStatement.executeUpdate()).thenReturn(1); // Erfolgreiche Ausführung

	    boolean result = service.deleteReimbursement(testReimb, testUser, false);
	    
	    assertTrue(result);
	}
	
	@Test
	void testRejectReimbursementWithNullInvoice() {
	    Reimbursement testReimb = new Reimbursement();
	    testReimb.setId(1);
	    assertThrows(NullPointerException.class, () -> {
	        service.rejectReimbursement(testReimb, testUser, false);
	    });
	}
}
