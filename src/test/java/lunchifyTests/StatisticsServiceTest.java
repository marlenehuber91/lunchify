package lunchifyTests;

import backend.logic.StatisticsService;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.User;
import backend.interfaces.ConnectionProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.sql.*;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StatisticsServiceTest {

    private StatisticsService statisticsService;

    @Mock
    private ConnectionProvider mockProvider;
    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;
    
    @Mock
    private User mockUser;
    @Mock
    private Invoice mockInvoice;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        
        // Set up the mock connection and provider
        when(mockProvider.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        
        // Set the connection provider for the service
        StatisticsService.setConnectionProvider(mockProvider);
        
        statisticsService = new StatisticsService();
    }

    @Test
    void testGraphDisplaysCorrectData() throws SQLException {
        // Arrange
        Reimbursement reimbursement1 = mock(Reimbursement.class);
        Reimbursement reimbursement2 = mock(Reimbursement.class);
        Reimbursement reimbursement3 = mock(Reimbursement.class);
        Reimbursement reimbursement4 = mock(Reimbursement.class);
        Reimbursement reimbursement5 = mock(Reimbursement.class);

        // Mocking the Invoice objects to avoid null pointer
        when(reimbursement1.getInvoice()).thenReturn(mockInvoice);
        when(reimbursement2.getInvoice()).thenReturn(mockInvoice);
        when(reimbursement3.getInvoice()).thenReturn(mockInvoice);
        when(reimbursement4.getInvoice()).thenReturn(mockInvoice);
        when(reimbursement5.getInvoice()).thenReturn(mockInvoice);

        // Mocking the category for each invoice
        when(mockInvoice.getCategory()).thenReturn(InvoiceCategory.RESTAURANT);
        when(reimbursement1.getInvoice().getCategory()).thenReturn(InvoiceCategory.RESTAURANT);
        when(reimbursement2.getInvoice().getCategory()).thenReturn(InvoiceCategory.RESTAURANT);
        when(reimbursement3.getInvoice().getCategory()).thenReturn(InvoiceCategory.RESTAURANT);
        when(reimbursement4.getInvoice().getCategory()).thenReturn(InvoiceCategory.SUPERMARKET);
        when(reimbursement5.getInvoice().getCategory()).thenReturn(InvoiceCategory.SUPERMARKET);

        List<Reimbursement> mockReimbursements = Arrays.asList(reimbursement1, reimbursement2, reimbursement3, reimbursement4, reimbursement5);
        statisticsService.setReimbursements(mockReimbursements);

        // Act
        Map<String, Integer> categoryDistribution = statisticsService.getCategoryDistribution(mockReimbursements);

        // Assert
        assertEquals(3, categoryDistribution.get("RESTAURANT"));
        assertEquals(2, categoryDistribution.get("SUPERMARKET"));
    }

    @Test
    void testFilterByMonth() {
        // Arrange
        LocalDate januaryDate = LocalDate.of(2025, 1, 15);
        LocalDate februaryDate = LocalDate.of(2025, 2, 20);

        Reimbursement reimbursement1 = mock(Reimbursement.class);
        Reimbursement reimbursement2 = mock(Reimbursement.class);
        when(reimbursement1.getInvoice().getDate()).thenReturn(januaryDate);
        when(reimbursement2.getInvoice().getDate()).thenReturn(februaryDate);

        List<Reimbursement> mockReimbursements = Arrays.asList(reimbursement1, reimbursement2);
        statisticsService.setReimbursements(mockReimbursements);

        // Act
        Map<String, Integer> invoiceCountPerMonth = statisticsService.getInvoiceCountPerMonth(mockReimbursements);

        // Assert
        assertEquals(1, invoiceCountPerMonth.get("January 2025"));
        assertEquals(1, invoiceCountPerMonth.get("February 2025"));
    }

    @Test
    void testHandleNoDataForSelectedFilter() {
        // Arrange
        LocalDate januaryDate = LocalDate.of(2025, 1, 15);
        LocalDate februaryDate = LocalDate.of(2025, 2, 20);

        Reimbursement reimbursement1 = mock(Reimbursement.class);
        Reimbursement reimbursement2 = mock(Reimbursement.class);
        when(reimbursement1.getInvoice().getDate()).thenReturn(januaryDate);
        when(reimbursement2.getInvoice().getDate()).thenReturn(februaryDate);

        List<Reimbursement> mockReimbursements = Arrays.asList(reimbursement1, reimbursement2);
        statisticsService.setReimbursements(mockReimbursements);

        // Act
        Map<String, Integer> invoiceCountPerMonth = statisticsService.getInvoiceCountPerMonth(mockReimbursements);

        // Test filter for a month with no data (e.g., March 2025)
        String selectedMonth = "March 2025";
        assertNull(invoiceCountPerMonth.get(selectedMonth)); // No data available for March 2025
    }

    @Test
    void testGetAverageInvoicesPerUserPerMonth() {
        // Arrange
        LocalDate januaryDate = LocalDate.of(2025, 1, 15);
        LocalDate februaryDate = LocalDate.of(2025, 2, 20);

        Reimbursement reimbursement1 = mock(Reimbursement.class);
        Reimbursement reimbursement2 = mock(Reimbursement.class);
        Reimbursement reimbursement3 = mock(Reimbursement.class);

        when(reimbursement1.getInvoice().getUser()).thenReturn(mockUser);
        when(reimbursement2.getInvoice().getUser()).thenReturn(mockUser);
        when(reimbursement3.getInvoice().getUser()).thenReturn(mockUser);

        when(reimbursement1.getInvoice().getDate()).thenReturn(januaryDate);
        when(reimbursement2.getInvoice().getDate()).thenReturn(februaryDate);
        when(reimbursement3.getInvoice().getDate()).thenReturn(februaryDate);

        List<Reimbursement> mockReimbursements = Arrays.asList(reimbursement1, reimbursement2, reimbursement3);
        statisticsService.setReimbursements(mockReimbursements);

        // Act
        Map<String, Double> avgInvoices = statisticsService.getAverageInvoicesPerUserPerMonth(mockReimbursements);

        // Assert
        assertEquals(1.5, avgInvoices.get(mockUser.getId() + "-2-2025"));
    }
    
    @Test
    void testStatisticsServiceThrowsException() {
        // Test for scenario when the service cannot work due to missing connection provider
        StatisticsService.setConnectionProvider(null);
        
        assertThrows(IllegalStateException.class, () -> {
            new StatisticsService().getSumByCategory();
        });
    }
}
