package uiTest;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.matcher.base.NodeMatchers.*;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;

import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import frontend.Main;
import frontend.controller.ReimbursementHistoryController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

@Tag("ui")
public class ReimbursementHistoryControllerTest extends ApplicationTest {

	private ReimbursementHistoryController controller;
	private ReimbursementService mockService;
   
	@Override
    public void start(Stage stage) throws Exception {
        Main mainApp = new Main();
        mainApp.setTestMode(true);
        mainApp.start(stage);
    }

    @BeforeEach
    void openReimbursementView() {
    	mockService = mock(ReimbursementService.class);
		controller = new ReimbursementHistoryController();
		controller.setReimbursementService(mockService);
    	clickOn("#reimbursementHistoryButton");
    }
    
    @BeforeEach
    void setupSessionUser() {
        SessionManager.setCurrentUser(new User(1, "Max Mustermann", "max@mustermann.at", UserRole.ADMIN, UserState.ACTIVE));
    }

    @Test //Done
    void testInitialUIElementsAreVisible() {
        verifyThat("#backButton", isVisible());
        verifyThat("#reimbursementHistoryTable", isVisible());
        verifyThat("#monthFilterBox", isVisible());
        verifyThat("#yearFilterBox", isVisible());
        verifyThat("#categoryFilterBox", isVisible());
        verifyThat("#statusFilterBox", isVisible());
        verifyThat("#resetFilterButton", isVisible());
        verifyThat("#totalReimbursementAmountLabel", isVisible());
    }
    
    @Test //Done
    void testInitialTotalLabelIsZero() {
        Label totalLabel = lookup("#totalReimbursementAmountLabel").query();
        assertTrue(totalLabel.getText().contains("€ 0.0"));
    }
    
    @Test
    void testFilterByMonth() { //DONE
        ComboBox<String> monthFilter = lookup("#monthFilterBox").query();
        clickOn("#monthFilterBox");
        clickOn("Jänner");
        
        verifyThat("#reimbursementHistoryTable", (TableView<Reimbursement> table) -> 
            table.getItems().stream().allMatch(r -> 
                r.getInvoice().getDate().getMonthValue() == 1));
    }

    @Test //Done
    void testFilterByYear() {
        ComboBox<String> yearFilter = lookup("#yearFilterBox").query();
        clickOn("#yearFilterBox");
        clickOn("2024");
        
        verifyThat("#reimbursementHistoryTable", (TableView<Reimbursement> table) -> 
            table.getItems().stream().allMatch(r -> 
                r.getInvoice().getDate().getYear() == 2024));
    }

    @Test //done
    void testFilterByCategory() {
        ComboBox<String> categoryFilter = lookup("#categoryFilterBox").query();
        clickOn("#categoryFilterBox");
        clickOn("Supermarkt");
        
        verifyThat("#reimbursementHistoryTable", (TableView<Reimbursement> table) -> 
            table.getItems().stream().allMatch(r -> 
                r.getInvoice().getCategory().toString().equals("SUPERMARKET")));
    }

    @Test //Done
    void testFilterByStatus() {
        ComboBox<String> statusFilter = lookup("#statusFilterBox").query();
        clickOn("#statusFilterBox");
        clickOn("genehmigt");
        
        verifyThat("#reimbursementHistoryTable", (TableView<Reimbursement> table) -> 
            table.getItems().stream().allMatch(r -> 
                r.getStatus() == ReimbursementState.APPROVED));
    }

    @Test //Done
    void testResetFilters() {
    	clickOn("#monthFilterBox");
        clickOn("Jänner");
        clickOn("#statusFilterBox");
        clickOn("genehmigt");
        
        clickOn("#resetFilterButton");
        
        ComboBox<String> monthFilter = lookup("#monthFilterBox").query();
        ComboBox<String> statusFilter = lookup("#statusFilterBox").query();
        
        Assertions.assertNull(monthFilter.getValue());
        Assertions.assertNull(statusFilter.getValue());
    }

    @Test //Done
    void testBackNavigation() {
        clickOn("#backButton");
        verifyThat("#adminDashboard", isVisible());
    }
}