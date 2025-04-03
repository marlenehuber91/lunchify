package lunchifyTests.lunchifyTests;

import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.control.LabeledMatchers.hasText;
import frontend.Main;

import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import backend.logic.InvoiceService;
import frontend.controller.AdminDashboardController;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class InvoiceUploadControllerTest extends ApplicationTest {

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
    void testSubmitButtonStaysDisabledForInvalidFields() {
        // Simuliere unvollständige Eingaben (z. B. nur Betrag eintragen)
        clickOn("#amountField");
        write("123.45");

        verifyThat("#submitButton", (Button button) -> button.isDisabled()); // Submit-Button bleibt deaktiviert
    }
	
	@Test
    void testSelectPreviousMonthDate() {
        clickOn("#datePicker");
        write("2025-03-15"); // Wähle ein Datum im vorherigen Monat
        
        verifyThat("#datePickerLabel", hasText("Datum eingeben")); // Label-Überprüfung
        verifyThat("#submitButton", (Button button) -> button.isDisabled());
    }
	
	@Test
	void testSelectFutureDate() {
		clickOn("#datePicker");
        write("2025-12-01"); // Zukunftsdatum eingeben
        
        verifyThat("#datePickerLabel", hasText("Datum eingeben")); // Label-Überprüfung
	}
	

}


