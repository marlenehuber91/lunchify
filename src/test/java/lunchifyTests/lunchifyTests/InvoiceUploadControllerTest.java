package lunchifyTests.lunchifyTests;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

import java.time.LocalDate;

import frontend.Main;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import backend.logic.InvoiceService;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.InvoiceState;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Assumptions;

import java.awt.GraphicsEnvironment;

public class InvoiceUploadControllerTest extends ApplicationTest {
	
    @BeforeEach
    public void setup() {
    	// skip tests in CI
        Assumptions.assumeTrue(!GraphicsEnvironment.isHeadless());
    }
	
	@Override
	public void start(Stage stage) {
		try {
			new Main().start(stage);
			clickOn("#openInvoiceUploadButton");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} // Starte deine JavaFX-Anwendung
	}

	@Test
	void testManualInvoiceAmountEntry() {
		clickOn("#amountField");
		write("150.00"); // Gültigen Betrag eingeben
		verifyThat("#amountLabel", hasText("Betrag eingegeben")); // Erfolgsmeldung überprüfen

	}

	@Test
	void testValidAmountNegativeFloat() {
		clickOn("#amountField");
		write("-150.00"); // Gültigen Betrag eingeben
		verifyThat("#amountLabel", hasText("Kein gültiger Zahlenwert")); // Erfolgsmeldung überprüfen

	}

	@Test
	void testValidAmount() {
		clickOn("#amountField");
		write("-sdf"); // Gültigen Betrag eingeben
		verifyThat("#amountLabel", hasText("Kein gültiger Zahlenwert")); // Erfolgsmeldung überprüfen

	}

	@Test
	void testSubmitButtonStaysDisabledForInvalidFields() {
		// Simuliere unvollständige Eingaben (z. B. nur Betrag eintragen)
		clickOn("#amountField");
		write("123.45");

		verifyThat("#submitButton", (Button button) -> button.isDisabled()); // Submit-Button bleibt deaktiviert
	}

	@Test
	void testSelectPreviousMonthDate() {
		clickOn("#datePicker");
		write("15.03.2025"); // Wähle ein Datum im vorherigen Monat

		verifyThat("#datePickerLabel", hasText("Datum eingeben")); // Label-Überprüfung
		verifyThat("#submitButton", (Button button) -> button.isDisabled());
	}

	@Test
	void testSelectFutureDate() {
		clickOn("#datePicker");
		write("31.12.2025"); // Zukunftsdatum eingeben

		verifyThat("#datePickerLabel", hasText("Datum eingeben")); // Label-Überprüfung
	}

	@Test
	void testInvoiceAlreadySubmittedForDay() {
		// Ein spezifisches Datum und einen Benutzer festlegen
		LocalDate testDate = LocalDate.of(2025, 4, 1); // Beispiel-Datum
		User testUser = new User("testUser", "test@domain.com", "password", UserRole.EMPLOYEE, UserState.ACTIVE);

		// Simuliere den Zustand, in dem bereits eine Rechnung für das Datum existiert
		InvoiceService invoiceService = new InvoiceService(testUser);
		invoiceService.addInvoice(
				new Invoice(testDate, 100.0f, InvoiceCategory.SUPERMARKET, InvoiceState.PENDING, null, testUser));

		// Überprüfen, ob die Methode `invoiceDateAlreadyUsed` true zurückgibt
		boolean isAlreadySubmitted = invoiceService.invoiceDateAlreadyUsed(testDate, testUser);
		assertTrue(isAlreadySubmitted,
				"Es sollte erkannt werden, dass bereits eine Rechnung für dieses Datum existiert.");
	}
}
