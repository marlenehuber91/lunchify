package uiTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

import java.io.File;
import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import backend.logic.SessionManager;
import backend.model.InvoiceCategory;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import frontend.Main;
import frontend.controller.BaseUploadController;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Tag("ui") //created by AI, changed by the team
public class BaseUploadControllerTest extends ApplicationTest {
	
	@Override
	public void start(Stage stage) throws Exception {
		Main mainApp = new Main();
		mainApp.setTestMode(true);
		mainApp.start(stage);
	}

	@BeforeEach
	void initialize() {
		clickOn("#openInvoiceUploadButton");
	}

	@Test
	void testInitialUIElementsAreVisible() {
		verifyThat("#datePicker", isVisible());
		verifyThat("#amountField", isVisible());
		verifyThat("#reimbursementAmountField", isVisible());
		verifyThat("#categoryBox", isVisible());
		verifyThat("#uploadPane", isVisible());
		verifyThat("#submitButton", isVisible());
	}

	@Test
	void testManualInvoiceAmountEntry() {
		clickOn("#amountField");
		write("150.00"); // Gültigen Betrag eingeben
		Label amountLabel = lookup("#amountLabel").query();
		Assertions.assertThat(amountLabel.getText()).contains("Betrag eingegeben");
	}

	@Test
	void testValidAmountNegativeFloat() {
		clickOn("#amountField");
		write("-150.00");
		Label amountLabel = lookup("#amountLabel").query();
		Assertions.assertThat(amountLabel.getText()).contains("Kein gültiger Zahlenwert");
	}

	@Test
	void testSubmitButtonStaysDisabledForInvalidFields() {
		clickOn("#amountField");
		write("123.45");
		verifyThat("#submitButton", (Button button) -> button.isDisabled()); // Submit-Button bleibt deaktiviert
	}

	@Test
	void testSelectPreviousMonthDate() {
		DatePicker datePicker = lookup("#datePicker").query();
		interact(() -> datePicker.setValue(LocalDate.of(2025, 3, 15)));

		Label datePickerLabel = lookup("#datePickerLabel").query();
		Assertions.assertThat(datePickerLabel.getText()).contains("Datum darf nicht vor dem aktuellen Monat liegen");

		verifyThat("#submitButton", (Button button) -> button.isDisabled());
	}

	@Test
	void testSelectFutureDate() {
		DatePicker datePicker = lookup("#datePicker").query();
		interact(() -> datePicker.setValue(LocalDate.of(2025, 12, 31)));

		Label datePickerLabel = lookup("#datePickerLabel").query();
		Assertions.assertThat(datePickerLabel.getText()).contains("Datum darf nicht in der Zukunft liegen");

		verifyThat("#submitButton", (Button button) -> button.isDisabled());
	}

	@Test
	void testInvoiceSubmissionWithMissingAmount() {
		// Nur Datum und Kategorie setzen, aber keinen Betrag eingeben
		clickOn("#datePicker");
		write("18.04.2025"); // simuliert aktuelle Tageswahl

		ComboBox<InvoiceCategory> comboBox = lookup("#categoryBox").query();
		interact(() -> {
			comboBox.setItems(FXCollections.observableArrayList(InvoiceCategory.RESTAURANT, InvoiceCategory.SUPERMARKET,
					InvoiceCategory.UNDETECTABLE));
		});
		clickOn("#categoryBox");
		clickOn("RESTAURANT");

		Button submitButton = lookup("#submitButton").query();
		Assertions.assertThat(submitButton.isDisable()).isTrue();
	}
	
	@Test
	void testSubmitButtonEnabledForValidInput() {
		clickOn("#amountField").write("20.00");

		DatePicker datePicker = lookup("#datePicker").query();
		interact(() -> datePicker.setValue(LocalDate.of(2025, 05, 02)));

		ComboBox<InvoiceCategory> comboBox = lookup("#categoryBox").query();
		interact(() -> {
			comboBox.setItems(FXCollections.observableArrayList(InvoiceCategory.RESTAURANT));
			comboBox.setValue(InvoiceCategory.RESTAURANT);
		});
		
		BaseUploadController controller = BaseUploadController.getInstance();
		File dummyFile = new File("src/test/resources/invalidInvoice.jpg");
		interact(() -> controller.setSelectedFile(dummyFile));
		
		// Simuliere Dateiupload durch direkten Aufruf (UI-unabhängig)
		interact(() -> {
			TextField field = lookup("#reimbursementAmountField").query();
			field.setText("3.00");
		});
		
		interact (()-> {
			TextField field = lookup("#amountField").query();
			field.setText("3.00");
		});

		verifyThat("#submitButton", isEnabled());
	}
	
	@Test
	void testDatePickerRejectsWeekend() {
		// Finde nächstes Wochenende (Samstag)
		LocalDate lastWeekend = LocalDate.now().minusDays((LocalDate.now().getDayOfWeek().getValue() + 1) % 7);

		DatePicker datePicker = lookup("#datePicker").query();
		interact(() -> datePicker.setValue(lastWeekend));

		Label datePickerLabel = lookup("#datePickerLabel").query();
		Assertions.assertThat(datePickerLabel.getText()).contains("Kein gültiger Arbeitstag");

		verifyThat("#submitButton", (Button b) -> b.isDisabled());
	}
	
	@Test
	void testNoCategorySelectedDisablesSubmitButton() {
		clickOn("#amountField").write("20.00");

		DatePicker datePicker = lookup("#datePicker").query();
		interact(() -> datePicker.setValue(LocalDate.now()));

		// Keine Kategorie ausgewählt
		verifyThat("#submitButton", (Button b) -> b.isDisabled());
	}
	
	@Test
	void testFileUploadValidFile() {
	    // Setze einen gültigen Dateipfad
	    BaseUploadController controller = BaseUploadController.getInstance();
	    File dummyFile = new File("src/main/resources/frontend/images/archiv.png");
	    interact(() -> controller.setSelectedFile(dummyFile));

	    // Überprüfe, dass die Datei korrekt ausgewählt wurde
	    assertEquals(dummyFile.getPath(), controller.getSelectedFile().getPath());
	}
}
