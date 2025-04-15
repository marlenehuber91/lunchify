package lunchifyTests;

import static org.junit.Assert.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import backend.logic.InvoiceService;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;

public class InvoiceServiceTest {

	private InvoiceService invoiceService;
    private User user;

    @Before
    public void setUp() {
		user = new User(1, "mockUser", "mock@lunch.at", UserRole.EMPLOYEE, UserState.ACTIVE);

        invoiceService = new InvoiceService();
        invoiceService.invoices = Arrays.asList(
                createInvoice(LocalDate.now().minusDays(1)),
                createInvoice(LocalDate.now().minusDays(2))
        );
    }

    private Invoice createInvoice(LocalDate date) {
        Invoice invoice = new Invoice();
        invoice.setDate(date);
        invoice.setAmount(10.0f);
        invoice.setCategory(InvoiceCategory.RESTAURANT);
        invoice.setUser(user);
        return invoice;
    }

    // === Date Validierung ===
    @Test
    public void testIsValidDate_withValidDate() {
        LocalDate validDate = LocalDate.now().minusDays(1); //muss je nach Tag an dem getestet wird angepasst werden
        assertTrue(invoiceService.isValidDate(validDate));
    }

    @Test
    public void testIsValidDate_withFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        assertFalse(invoiceService.isValidDate(futureDate));
    }

    @Test
    public void testIsValidDate_withDifferentMonth() {
        LocalDate oldDate = LocalDate.now().minusMonths(1);
        assertFalse(invoiceService.isValidDate(oldDate));
    }

    @Test
    public void testIsValidDate_withWeekend() {
        // Finde nächstes Wochenende
        LocalDate saturday = LocalDate.now().with(DayOfWeek.SATURDAY);
        assertFalse(InvoiceService.isWorkday(saturday));
        assertFalse(invoiceService.isValidDate(saturday));
    }

    @Test
    public void testIsWorkday_forWeekday() {
        LocalDate monday = LocalDate.now().with(DayOfWeek.MONDAY);
        assertTrue(InvoiceService.isWorkday(monday));
    }

    // === invoiceDateAlreadyUsed ===
    @Test
    public void testInvoiceDateAlreadyUsed_found() {
        LocalDate existingDate = invoiceService.invoices.get(0).getDate();
        assertTrue(invoiceService.invoiceDateAlreadyUsed(existingDate, user));
    }

    @Test
    public void testInvoiceDateAlreadyUsed_notFound() {
        LocalDate newDate = LocalDate.of(2000, 1, 1);
        assertFalse(invoiceService.invoiceDateAlreadyUsed(newDate, user));
    }

    // === Float-Validierung ===
    @Test
    public void testIsValidFloat() {
        assertTrue(invoiceService.isValidFloat("123.45"));
        assertTrue(invoiceService.isValidFloat("100"));
        assertFalse(invoiceService.isValidFloat("12,34"));
        assertFalse(invoiceService.isValidFloat("abc"));
    }

    @Test
    public void testIsAmountValid() {
        assertTrue(invoiceService.isamaountValid("15.00"));
        assertFalse(invoiceService.isamaountValid("notANumber"));
        assertFalse(invoiceService.isamaountValid(null));
    }

    // === getInvoices ===
    @Test
    public void testGetInvoices() {
        assertEquals(2, invoiceService.getInvoices().size());
    }
}
