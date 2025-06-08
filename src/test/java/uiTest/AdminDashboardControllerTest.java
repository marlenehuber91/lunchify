package uiTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.isVisible;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import backend.logic.SessionManager;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import frontend.Main;
import javafx.scene.text.Text;
import javafx.stage.Stage;

@Tag("ui")
public class AdminDashboardControllerTest extends ApplicationTest {

    @Override
    public void start(Stage stage) throws Exception {
        Main mainApp = new Main();
        mainApp.setTestMode(true);
        mainApp.start(stage);
    }

    @BeforeEach
    void initialize() {
        SessionManager.setCurrentUser(new User(1, "TestUser", "mock@lunch.at", UserRole.ADMIN, UserState.ACTIVE));
    }
    
    @AfterEach
    void tearDown() {
    	SessionManager.setCurrentUser(null);
    }

    @Test
    void testInitialUIElementsAreVisible() {
        verifyThat("#userNameText", isVisible());
        verifyThat("#openInvoiceUploadButton", isVisible());
        verifyThat("#reimbursementHistoryButton", isVisible());
        verifyThat("#openCurrReimbursements", isVisible());
        verifyThat("#modifyUsersButton", isVisible());
    }

    @Test
    void testUserNameDisplay() {
        Text userNameText = lookup("#userNameText").query();
        assertEquals("Hallo, TestUser!", userNameText.getText());
    }

    @Test
    void testOpenInvoiceSubmissionWindow() {
        clickOn("#openInvoiceUploadButton");
        verifyThat("#datePicker", isVisible());
		verifyThat("#amountField", isVisible());
		verifyThat("#reimbursementAmountField", isVisible());
		verifyThat("#categoryBox", isVisible());
		verifyThat("#uploadPane", isVisible());
		verifyThat("#submitButton", isVisible());
    }

    @Test
    void testOpenCurrentReimbursementWindow() {
        clickOn("#openCurrReimbursements");
        verifyThat("#backArrow", isVisible());
        verifyThat("#currentMonthText", isVisible());
        verifyThat("#currReimbursementTable", isVisible());
        verifyThat("#totalReimbursementAmountLabel", isVisible());
        
    }

    @Test
    void testOpenReimbursementHistoryWindow() {
        clickOn("#openCurrReimbursements");
        verifyThat("#backArrow", isVisible());
    }
    
    //TODO: extend testcases
}
