package frontend.controller;

import javafx.scene.input.MouseEvent;

public class UserDashboardController {

    AdminDashboardController adminController = new AdminDashboardController();

    public void openInvoiceSubmissionWindow(MouseEvent event) {
        adminController.onklickOpenInvoiceSubmissionWindow(event);
    }
}