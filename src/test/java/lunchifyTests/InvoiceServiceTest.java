package lunchifyTests;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import backend.interfaces.ConnectionProvider;
import backend.logic.InvoiceService;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;

public class InvoiceServiceTest {

	private InvoiceService invoiceService;
    private User user;

    @Before
    public void setUp() {
		user = new User(1, "mockUser", "mock@lunch.at", UserRole.EMPLOYEE, UserState.ACTIVE);

        invoiceService = new InvoiceService(user);
        invoiceService.invoices = Arrays.asList(
                createInvoice(LocalDate.now().minusDays(1)),
                createInvoice(LocalDate.now().minusDays(2))
        );
    }
    
    @After
    public void tearDown() {
        InvoiceService.setConnectionProvider(null); // Statischen Zustand zurücksetzen
    }

    private Invoice createInvoice(LocalDate date) {
        Invoice invoice = new Invoice();
        invoice.setDate(date);
        invoice.setAmount(10.0f);
        invoice.setCategory(InvoiceCategory.RESTAURANT);
        invoice.setUser(user);
        return invoice;
    }


    @Test
    public void testIsValidDateWithValidDate() {
        LocalDate validDate = LocalDate.of(2025, 05, 02); //muss je nach Monat an dem getestet wird angepasst werden
        assertTrue(invoiceService.isValidDate(validDate));
    }

    @Test
    public void testIsValidDateWithFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        assertFalse(invoiceService.isValidDate(futureDate));
    }

    @Test
    public void testIsValidDateWithDifferentMonth() {
        LocalDate oldDate = LocalDate.now().minusMonths(1);
        assertFalse(invoiceService.isValidDate(oldDate));
    }

    @Test
    public void testIsValidDateWithWeekend() {
        // Finde nächstes Wochenende
        LocalDate saturday = LocalDate.now().with(DayOfWeek.SATURDAY);
        assertFalse(InvoiceService.isWorkday(saturday));
        assertFalse(invoiceService.isValidDate(saturday));
    }

    @Test
    public void testIsWorkdayForWeekday() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        assertTrue(InvoiceService.isWorkday(monday));
    }


    @Test
    public void testInvoiceDateAlreadyUsedFound() {
        LocalDate existingDate = invoiceService.invoices.get(0).getDate();
        assertTrue(invoiceService.invoiceDateAlreadyUsed(existingDate, user));
    }

    @Test
    public void testInvoiceDateAlreadyUsedNotFound() {
        LocalDate newDate = LocalDate.of(2000, 1, 1);
        assertFalse(invoiceService.invoiceDateAlreadyUsed(newDate, user));
    }
    
    @Test
    public void dateIsNull () {
    	LocalDate newDate = null;
    	assertFalse(invoiceService.isValidDate(newDate));
    }

    @Test
    public void testIsValidFloat() {
        assertTrue(invoiceService.isValidFloat("123.45"));
        assertTrue(invoiceService.isValidFloat("100"));
        assertFalse(invoiceService.isValidFloat("12,34"));
        assertFalse(invoiceService.isValidFloat("abc"));
        assertFalse(invoiceService.isValidFloat("-1"));
    }

    @Test
    public void testIsAmountValid() {
        assertTrue(invoiceService.isAmountValid("15.00"));
        assertFalse(invoiceService.isAmountValid("notANumber"));
        assertFalse(invoiceService.isAmountValid(null));
    }

    @Test
    public void testGetInvoices() {
        assertEquals(2, invoiceService.getInvoices().size());
    }
    
    @Test
    public void testGetAllInvoicesReturnsInvoices() throws Exception {
        
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(42);

        ConnectionProvider mockProvider = mock(ConnectionProvider.class);
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        // Setze den mockProvider in der Serviceklasse
        InvoiceService.setConnectionProvider(mockProvider);
        when(mockProvider.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        // Simuliere ein ResultSet mit einer Rechnung
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getFloat("amount")).thenReturn(99.99f);
        when(mockResultSet.getString("category")).thenReturn("RESTAURANT");
        when(mockResultSet.getDate("date")).thenReturn(Date.valueOf("2024-04-01"));

        // Act
        List<Invoice> result = InvoiceService.getAllInvoices(mockUser);

        // Assert
        assertEquals(1, result.size());
        Invoice invoice = result.get(0);
        assertEquals(1, invoice.getId());
        assertEquals(99.99f, invoice.getAmount());
        assertEquals(InvoiceCategory.RESTAURANT, invoice.getCategory());
        assertEquals(LocalDate.of(2024, 4, 1), invoice.getDate());
    }


    @Test
    public void testLoadInvoice() throws Exception {
        // Arrange
        Reimbursement reimbursement = mock(Reimbursement.class);
        when(reimbursement.getId()).thenReturn(1);

        // Mock Datenbank-Interaktionen
        ConnectionProvider mockProvider = mock(ConnectionProvider.class);
        Connection mockConnection = mock(Connection.class);
        PreparedStatement mockStatement = mock(PreparedStatement.class);
        ResultSet mockResultSet = mock(ResultSet.class);

        when(mockProvider.getConnection()).thenReturn(mockConnection);
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);

        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getFloat("amount")).thenReturn(99.99f);
        when(mockResultSet.getString("category")).thenReturn("RESTAURANT");
        when(mockResultSet.getDate("date")).thenReturn(Date.valueOf("2024-04-01"));

        // Setze den mockProvider in der Serviceklasse
        InvoiceService.setConnectionProvider(mockProvider);

        // Act
        Invoice result = invoiceService.loadInvoice(reimbursement);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getId());
        assertEquals(99.99f, result.getAmount());
        assertEquals(InvoiceCategory.RESTAURANT, result.getCategory());
        assertEquals(LocalDate.of(2024, 4, 1), result.getDate());
    }
       
    @Test
    public void testIsValidDateWithNullDate() {
        LocalDate nullDate = null;

        assertFalse(invoiceService.isValidDate(nullDate));
    }
    
    @Test
    public void testGetInvoiceByIdNotFound() throws Exception {
        ConnectionProvider mockProvider = mock(ConnectionProvider.class);
        Connection mockConn = mock(Connection.class);
        PreparedStatement stmt = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(mockProvider.getConnection()).thenReturn(mockConn);
        when(mockConn.prepareStatement(anyString())).thenReturn(stmt);
        when(stmt.executeQuery()).thenReturn(rs);
        when(rs.next()).thenReturn(false);

        InvoiceService.setConnectionProvider(mockProvider);

        Invoice invoice = InvoiceService.getInvoiceById(9999);
        assertNull(invoice);
    }
    
    @Test
    public void testInvoiceDateAlreadyUsedWithExclude() {
        invoiceService.invoices = Arrays.asList(
            createInvoice(LocalDate.of(2025, 5, 10)),
            createInvoice(LocalDate.of(2025, 5, 11))
        );
        long excludeId = invoiceService.invoices.get(0).getId();

        boolean result = invoiceService.invoiceDateAlreadyUsed(LocalDate.of(2025, 5, 10), user, excludeId);
        assertFalse(result);

        result = invoiceService.invoiceDateAlreadyUsed(LocalDate.of(2025, 5, 10), user, -1);
        assertTrue(result);
    }
    
    @Test
    public void testAddInvoiceFailsWhenSQLException() throws Exception {
        Invoice invoice = new Invoice();
        invoice.setDate(LocalDate.now());
        invoice.setAmount(10.0f);
        invoice.setCategory(InvoiceCategory.RESTAURANT);
        invoice.setUser(user);

        ConnectionProvider mockProvider = mock(ConnectionProvider.class);
        when(mockProvider.getConnection()).thenThrow(new SQLException("Connection error"));

        InvoiceService.setConnectionProvider(mockProvider);

        boolean result = InvoiceService.addInvoice(invoice);
        assertFalse(result);
    }
    /*
     * no further testing, because we would have to mock a lot of objects
     */
}
