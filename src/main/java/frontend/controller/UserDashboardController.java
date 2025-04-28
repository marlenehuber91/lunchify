package frontend.controller;

import backend.logic.SessionManager;
import backend.model.User;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;

public class UserDashboardController {
    @FXML
    private Text userNameText;

    @FXML
    public void initialize() {

        User user = SessionManager.getCurrentUser();
        if (user != null) {
            String username = user.getName();
            userNameText.setText("Hallo, " + username + "!");
        } else {
            userNameText.setText("Nicht eingeloggt");
        }
    }

    public AdminDashboardController adminController = new AdminDashboardController();

    public void openInvoiceSubmissionWindow(MouseEvent event) {
        adminController.onklickOpenInvoiceSubmissionWindow(event);
    }
    
    public void openCurrentReimbursement(MouseEvent event) {
    	adminController.openCurrentReimbursement(event);
    }
    
    public void openReimbursementHistory(MouseEvent event) {
    	adminController.openReimbursementHistory(event);
    }

}
