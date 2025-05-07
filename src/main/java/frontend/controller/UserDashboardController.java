package frontend.controller;

import java.util.List;

import backend.logic.NotificationService;
import backend.logic.SessionManager;
import backend.model.Notification;
import backend.model.User;
import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

public class UserDashboardController {
    @FXML
    private Text userNameText;
    
    @FXML
    Circle redDot;
    
    @FXML
    public void initialize() {

        User user = SessionManager.getCurrentUser();
        if (user != null) {
            String username = user.getName();
            userNameText.setText("Hallo, " + username + "!");
        } else {
            userNameText.setText("Nicht eingeloggt");
        }
        
        List<Notification> notifications = NotificationService.getNotificationsByUser(user);
        redDot.setVisible(NotificationService.hasUnreadNotifications(notifications, user.getId()));
        
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
    
    public void openStatistics(MouseEvent event) {
    	adminController.openStatistics(event);
    }
    
    public void openNotifications(MouseEvent event) {
    	adminController.openNotifications(event);
    }
}
