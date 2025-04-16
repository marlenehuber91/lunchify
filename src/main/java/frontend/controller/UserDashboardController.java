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
    void initialize() {

        User user = SessionManager.getCurrentUser();
        if (user != null) {
            String username = user.getName();
            userNameText.setText("Hallo, " + username + "!");
        } else {
            userNameText.setText("Nicht eingeloggt");
        }
    }

    AdminDashboardController adminController = new AdminDashboardController();

    public void openInvoiceSubmissionWindow(MouseEvent event) {
        adminController.onklickOpenInvoiceSubmissionWindow(event);
    }

    @FXML
    public void onClickOpenModifyReimbursementWindow(MouseEvent mouseEvent) {
        adminController.onClickOpenModifyReimbursementWindow(mouseEvent);
    }

    @FXML
    void openCurrentReimbursement(MouseEvent event) {
        adminController.openCurrentReimbursement(event);
    }


}