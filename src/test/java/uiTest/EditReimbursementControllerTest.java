package uiTest;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.testfx.framework.junit5.ApplicationTest;

import backend.model.InvoiceCategory;
import frontend.controller.EditReimbursementController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

@Tag("ui")
public class EditReimbursementControllerTest extends ApplicationTest {

	private EditReimbursementController controller;

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/views/editReimbursement.fxml"));
        Parent root = loader.load();
        controller = loader.getController();

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @BeforeEach
    public void setup() {
    	MockitoAnnotations.openMocks(this);
        interact(() -> {
        	((ComboBox<InvoiceCategory>) lookup("#categoryBox").query()).getItems().setAll(InvoiceCategory.values());
            ((DatePicker) lookup("#datePicker").query()).setValue(LocalDate.now());
        });
    }

    @Test
    public void testInitialValuesAreEmpty() {
        TextField amountField = lookup("#amountField").query();
        TextField reimbursementAmountField = lookup("#reimbursementAmountField").query();
        ComboBox<?> categoryBox = lookup("#categoryBox").query();

        assertEquals("", amountField.getText());
        assertEquals("", reimbursementAmountField.getText());
        assertNull(categoryBox.getValue());
    }

    @Test
    public void testUserCanInputAmount() {
        clickOn("#amountField").write("45.67");
        TextField amountField = lookup("#amountField").query();
        assertEquals("45.67", amountField.getText());
    }

    @Test
    public void testUserCanSelectCategory() {
        ComboBox<InvoiceCategory> comboBox = lookup("#categoryBox").query();
        interact(() -> comboBox.getSelectionModel().select(InvoiceCategory.RESTAURANT));
        assertEquals(InvoiceCategory.RESTAURANT, comboBox.getValue());
    }

    @Test
    public void testUserCanInputInvoiceAmount() {
        clickOn("#amountField").write("25.00");
        TextField reimbursementAmountField = lookup("#amountField").query();
        assertEquals("25.00", reimbursementAmountField.getText());
    }

    @Test
    public void testUserCanSetDate() {
        DatePicker datePicker = lookup("#datePicker").query();
        LocalDate date = LocalDate.now().minusDays(1);
        interact(() -> datePicker.setValue(date));
        assertEquals(date, datePicker.getValue());
    }
}
