package uiTest;

import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.model.*;
import frontend.Main;
import frontend.controller.ModifyReimbursementController;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import static org.mockito.Mockito.mock;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

@Tag("ui")
public class ModifyReimbursementControllerTest extends ApplicationTest {

	private ModifyReimbursementController controller = new ModifyReimbursementController();
	private ReimbursementService mockService = mock(ReimbursementService.class);
   
	@Override
    public void start(Stage stage) throws Exception {
        Main mainApp = new Main();
        mainApp.setTestMode(true);
        mainApp.start(stage);
    }

    @BeforeEach
    void openReimbursementView() {
    	mockService = mock(ReimbursementService.class);
		controller = new ModifyReimbursementController();
    	clickOn("#openModifyReimbursmentAmountButton");
    }
    
    @BeforeEach
    void setupSessionUser() {
        SessionManager.setCurrentUser(new User(1, "Max Mustermann", "max@mustermann.at", UserRole.ADMIN, UserState.ACTIVE));
    }

    @Test //Done
    void testInitialUIElementsAreVisible() {
        verifyThat("#backButton", isVisible());
        verifyThat("#currentAmountField", isVisible());
        verifyThat("#categoryBox", isVisible());
        verifyThat("#newAmountField", isVisible());
        verifyThat("#amountLabel", isVisible());
    }

    @Test //done
    void testComboBoxCategory() {
        ComboBox<InvoiceCategory> categoryBox = lookup("#categoryBox").query();
        clickOn("#categoryFilterBox");
        clickOn("Supermarket");
        
        verifyThat("#currentAmountField", isVisible());
    }

    @Test //Done
    void testBackNavigation() {
        clickOn("#backButton");
        verifyThat("#adminDashboard", isVisible());
    }

}

