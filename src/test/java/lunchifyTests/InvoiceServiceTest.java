package lunchifyTests;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
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
    public void testAddInvoiceWithoutFileShouldReturnTrueAndSetId() throws Exception {
        // Arrange
        Invoice invoice = new Invoice();
        invoice.setDate(LocalDate.of(2024, 4, 1));
        invoice.setAmount(99.99f);
        invoice.setCategory(InvoiceCategory.RESTAURANT);
        invoice.setFile(null);
        invoice.setFlag(false);

        User user = mock(User.class);
        when(user.getId()).thenReturn(1);
        invoice.setUser(user);

        // Mocks vorbereiten
        ConnectionProvider mockProvider = mock(ConnectionProvider.class);
        Connection mockConn = mock(Connection.class);

        // PreparedStatement und ResultSet für Flag-Abfrage (permFlagStmt)
        PreparedStatement mockPermFlagStmt = mock(PreparedStatement.class);
        ResultSet mockPermFlagRs = mock(ResultSet.class);

        // PreparedStatement und ResultSet für Insert (stmt)
        PreparedStatement mockStmt = mock(PreparedStatement.class);
        ResultSet mockKeys = mock(ResultSet.class);

        // Verknüpfungen setzen
        when(mockProvider.getConnection()).thenReturn(mockConn);

        // Für permFlagStmt: PreparedStatement ohne RETURN_GENERATED_KEYS
        when(mockConn.prepareStatement("SELECT permanent_flag FROM FlaggedUsers WHERE user_id = ?"))
                .thenReturn(mockPermFlagStmt);
        when(mockPermFlagStmt.executeQuery()).thenReturn(mockPermFlagRs);
        when(mockPermFlagRs.next()).thenReturn(false); // User ist nicht permanent flagged

        // Für stmt: PreparedStatement mit RETURN_GENERATED_KEYS (Insert)
        when(mockConn.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockStmt);
        when(mockStmt.executeUpdate()).thenReturn(1); // simulate success
        when(mockStmt.getGeneratedKeys()).thenReturn(mockKeys);
        when(mockKeys.next()).thenReturn(true);
        when(mockKeys.getInt(1)).thenReturn(42);

        // ConnectionProvider setzen
        InvoiceService.setConnectionProvider(mockProvider);

        // Act
        boolean result = InvoiceService.addInvoice(invoice);

        // Assert
        assertTrue(result);
        assertEquals(42, invoice.getId());

        // Verify relevante Methodenaufrufe am Insert-PreparedStatement
        verify(mockStmt).setDate(eq(1), any());
        verify(mockStmt).setFloat(2, 99.99f);
        verify(mockStmt).setObject(3, InvoiceCategory.RESTAURANT, Types.OTHER);
        verify(mockStmt).setInt(4, 1);
        verify(mockStmt).setNull(5, Types.BINARY); // weil file == null
        verify(mockStmt).setBoolean(6, false);
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
}
