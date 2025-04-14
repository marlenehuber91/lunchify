package lunchifyTests;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;

import java.time.LocalDate;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import backend.logic.InvoiceService;
import backend.logic.SessionManager;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.ReimbursementState;
import backend.model.User;
import frontend.Main;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


public class InvoiceUploadControllerTest extends ApplicationTest {

	@Override
	public void start(Stage stage) {
		System.setProperty("testfx.robot", "glass");
		System.setProperty("testfx.headless", "true");
		System.setProperty("prism.order", "sw");
		System.setProperty("prism.text", "t2k");
		System.setProperty("java.awt.headless", "true");
		try {
			Main main = new Main();
			main.setTestMode(true);
			main.start(stage);
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
       User testUser = SessionManager.getCurrentUser();

       // Simuliere den Zustand, in dem bereits eine Rechnung für das Datum existiert
       InvoiceService invoiceService = new InvoiceService(testUser);
       invoiceService.addInvoice(
             new Invoice(testDate, 100.0f, InvoiceCategory.SUPERMARKET, null, testUser));
       
       // Überprüfen, ob die Methode `invoiceDateAlreadyUsed` true zurückgibt
       InvoiceService invoiceService2 = new InvoiceService(testUser);
       boolean isAlreadySubmitted = invoiceService2.invoiceDateAlreadyUsed(testDate, testUser);
       assertTrue(isAlreadySubmitted,
             "Es sollte erkannt werden, dass bereits eine Rechnung für dieses Datum existiert.");
    }
    
    @Test
    void testReimbursementAmountRestaurantMoreThanLimit () {
       clickOn("#amountField");
       write("4");
       clickOn("#categoryBox");
       clickOn("RESTAURANT");
       TextField field = lookup("#reimbursementAmountField").query();
       assertEquals("3.0", field.getText());
    }
    
    @Test
    void testReimbursementAmountRestaurantLessThanLimit () {
       clickOn("#amountField");
       write("2.50");
       clickOn("#categoryBox");
       clickOn("RESTAURANT");
       TextField field = lookup("#reimbursementAmountField").query();
       assertEquals("2.5", field.getText());
    }
    

    @Test
    void testReimbursementAmountSupermarketMoreThanLimit () {
       clickOn("#amountField");
       write("3");
       clickOn("#categoryBox");
       clickOn("SUPERMARKET");
       TextField field = lookup("#reimbursementAmountField").query();
       assertEquals("2.5", field.getText());
    }
    
    @Test
    void testReimbursementAmountSupermarketLessThanLimit () {
       clickOn("#amountField");
       write("1.50");
       clickOn("#categoryBox");
       clickOn("SUPERMARKET");
       TextField field = lookup("#reimbursementAmountField").query();
       assertEquals("1.5", field.getText());
    }
    
    @Test
    void testReimbursementChangeCategory () {
       clickOn("#amountField");
       write("2.75");
       clickOn("#categoryBox");
       clickOn("SUPERMARKET");
       TextField field = lookup("#reimbursementAmountField").query();
       assertEquals("2.5", field.getText());
       
       clickOn("#categoryBox");
       clickOn("RESTAURANT");
       field = lookup("#reimbursementAmountField").query();
       assertEquals("2.75", field.getText());
       
    }
}

