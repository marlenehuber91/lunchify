package uiTest;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.contains;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

import java.io.File;
import java.time.LocalDate;
import java.util.List;
import javafx.scene.Node;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationTest;

import com.sun.jdi.Field;

import backend.logic.SessionManager;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.ReimbursementState;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import frontend.Main;
import frontend.controller.InvoiceUploadController;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

@Tag("ui")
public class InvoiceUploadControllerTest extends ApplicationTest {
	
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
	void testBackNavigation() {
		SessionManager.setCurrentUser(new User (0,"dummy", "dummy@lunch.at", UserRole.ADMIN, UserState.ACTIVE));
		clickOn("#backButton");
	}

	@Test
	void testValidationMissingFields() {
		clickOn("#submitButton");
		verifyThat("#amountLabel", isVisible());
	}

	@Test
	void testReimbursementAmountRestaurantMoreThanLimit() {
		clickOn("#amountField");
		write("4");
		clickOn("#categoryBox");
		clickOn("RESTAURANT");
		TextField field = lookup("#reimbursementAmountField").query();
		assertEquals("3.0", field.getText());
	}

	@Test
	void testReimbursementAmountRestaurantLessThanLimit() {
		clickOn("#amountField");
		write("2.50");
		clickOn("#categoryBox");
		clickOn("RESTAURANT");
		TextField field = lookup("#reimbursementAmountField").query();
		assertEquals("2.5", field.getText());
	}

	@Test
	void testReimbursementAmountSupermarketMoreThanLimit() {
		clickOn("#amountField");
		write("3");
		clickOn("#categoryBox");
		clickOn("SUPERMARKET");
		TextField field = lookup("#reimbursementAmountField").query();
		assertEquals("2.5", field.getText());
	}

	@Test
	void testReimbursementAmountSupermarketLessThanLimit() {
		clickOn("#amountField");
		write("1.50");
		clickOn("#categoryBox");
		clickOn("SUPERMARKET");
		TextField field = lookup("#reimbursementAmountField").query();
		assertEquals("1.5", field.getText());
	}

	@Test
	void testReimbursementChangeCategory() {
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
