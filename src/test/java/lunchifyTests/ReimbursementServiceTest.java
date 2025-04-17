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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
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


}
