package uiTest;

import backend.logic.ReimbursementService;
import backend.model.*;
import frontend.Main;
import frontend.controller.CurrReimbursementController;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Tag("ui")
public class CurrReimbursementControllerTest extends ApplicationTest {
	
	private CurrReimbursementController controller;
    private ReimbursementService mockService;

	@Override
	public void start(Stage stage) throws Exception {
		Main mainApp = new Main();
		mainApp.setTestMode(true);
		mainApp.start(stage);
	}

	@BeforeEach
	void openReimbursementView() {
		clickOn("#openCurrReimbursements");
		mockService = mock(ReimbursementService.class);
		controller = new CurrReimbursementController();
		controller.setReimbursementService(mockService);
	}
	
	@Test // Done
	public void testTotalReimbursementLabelEmpty() {
		Label totalLabel = lookup("#totalReimbursementAmountLabel").query();
		assertNotNull(totalLabel);
		assertTrue(totalLabel.getText().contains("€ 0.0"));
	}

	@Test // Done
	public void testCurrentMonthTextCorrect() {
		Text monthText = lookup("#currentMonthText").query();
		assertNotNull(monthText);

		String expectedMonth = LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());

		assertTrue(monthText.getText().contains(expectedMonth));
	}
	//TODO: extend tescases
}
