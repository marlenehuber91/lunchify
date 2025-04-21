package lunchifyTests;

import backend.logic.ReimbursementService;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import backend.interfaces.ConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReimbursementServiceTest {

    @Mock private Connection mockConnection;
    @Mock private PreparedStatement mockStatement;
    @Mock private ResultSet mockResultSet;

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
        Reimbursement r1 = new Reimbursement();
        r1.setApprovedAmount(3.0f);
        r1.setStatus(ReimbursementState.APPROVED);

        Reimbursement r2 = new Reimbursement();
        r2.setApprovedAmount(2.0f);
        r2.setStatus(ReimbursementState.REJECTED);

        float total = service.getTotalReimbursement(Arrays.asList(r1, r2));
        assertEquals(3.0f, total);
    }

    @Test
    void testGetTotalReimbursementByState() {
        Reimbursement r1 = new Reimbursement();
        r1.setApprovedAmount(2.0f);
        r1.setStatus(ReimbursementState.PENDING);

        Reimbursement r2 = new Reimbursement();
        r2.setApprovedAmount(3.0f);
        r2.setStatus(ReimbursementState.PENDING);

        float total = service.getTotalReimbursement(Arrays.asList(r1, r2), ReimbursementState.PENDING);
        assertEquals(5.0f, total);
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
        List<Reimbursement> result = service.getFilteredReimbursements("April", "2023", "RESTAURANT", "APPROVED");
        assertNotNull(result);
    }
    
    @Test
    void testGetAllReimbursements() {
        List<Reimbursement> result = service.getAllReimbursements();
        assertNotNull(result);
    }

    @Test
    void testGetCurrentReimbursements() {
        List<Reimbursement> result = service.getCurrentReimbursements();
        assertNotNull(result);
    }
    
    @Test
    void testGetTotalReimbursementEmptyList() {
    	float total = service.getTotalReimbursement(List.of());
    	assertEquals(0.0f, total);
    }
    
    @Test
    void testGetTotalReimbursementAllRejected() {
        Reimbursement r1 = new Reimbursement();
        r1.setApprovedAmount(1.0f);
        r1.setStatus(ReimbursementState.REJECTED);

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
        Reimbursement r1 = new Reimbursement();
        r1.setApprovedAmount(2.0f);
        r1.setStatus(ReimbursementState.REJECTED);

        float total = service.getTotalReimbursement(List.of(r1), ReimbursementState.APPROVED);
        assertEquals(0.0f, total);
    }
    
    @Test
    void testGetFilteredReimbursementsWithAlleMonth() {
        List<Reimbursement> result = service.getFilteredReimbursements("alle", "2023", "RESTAURANT", "APPROVED");
        assertNotNull(result);
    }
    
    @Test //created by AI
    void testAddReimbursement_ConnectionProviderNull_shouldThrowException() {
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

        when(mockResultSet.getInt("invoice_id")).thenReturn(1);
        when(mockResultSet.getFloat("invoiceAmount")).thenReturn(10.0f);
        when(mockResultSet.getString("category")).thenReturn("RESTAURANT");
        when(mockResultSet.getDate("date")).thenReturn(Date.valueOf(LocalDate.now()));

        List<Reimbursement> reimbursements = service.getAllReimbursements();

        assertEquals(1, reimbursements.size());
        assertEquals(ReimbursementState.PENDING, reimbursements.get(0).getStatus());
    }
    
    @Test // created by ChatGPT corrected
    void testGetFilteredReimbursementsReturnsCorrectItems() throws SQLException {
  
        when(mockResultSet.next()).thenReturn(true, false);
        
        when(mockResultSet.getString("category")).thenReturn("RESTAURANT");
        when(mockResultSet.getString("status")).thenReturn("APPROVED");
        when(mockResultSet.getFloat("approved_amount")).thenReturn(5.0f);
        when(mockResultSet.getDate("processed_date")).thenReturn(Date.valueOf("2023-04-10"));
        when(mockResultSet.getDate("date")).thenReturn(Date.valueOf("2023-04-01"));

        when(mockResultSet.getInt("reimbId")).thenReturn(1);
        when(mockResultSet.getInt("invoice_id")).thenReturn(10);
        when(mockResultSet.getFloat("invoiceAmount")).thenReturn(10.0f);

        // Methode aufrufen mit Filter für April 2023, Kategorie RESTAURANT, Status APPROVED
        List<Reimbursement> filtered = service.getFilteredReimbursements("April", "2023", "RESTAURANT", "APPROVED");

        // Erwartet: genau 1 Eintrag
        assertEquals(1, filtered.size());
        assertEquals("RESTAURANT", filtered.get(0).getInvoice().getCategory().toString());
        assertEquals(ReimbursementState.APPROVED, filtered.get(0).getStatus());
    }    
}
