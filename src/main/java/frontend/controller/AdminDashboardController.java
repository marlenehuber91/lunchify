package frontend.controller;
import java.io.IOException;
import java.util.List;

import backend.logic.NotificationService;
import backend.logic.SessionManager;
import backend.model.Notification;
import backend.model.User;
import backend.model.UserRole;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Node;


public class AdminDashboardController {

    @FXML
    private Text userNameText;
    
    @FXML
    private Circle redDot;
    

    @FXML
    public void initialize() {
    	redDot.setVisible(false);
    	User user = SessionManager.getCurrentUser();
        if (user != null) {
            String username = user.getName();
            userNameText.setText("Hallo, " + username + "!");
        } else {
            userNameText.setText("Nicht eingeloggt");
        }
        
        List<Notification> notifications = NotificationService.getNotificationsByUser(user);
        redDot.setVisible(NotificationService.hasUnreadNotifications(notifications, user.getId()));
        
      	if (user.getRole().equals(UserRole.ADMIN)) {
      		List<Notification> adminNotifications = NotificationService.getAdminNotification();
      		redDot.setVisible(NotificationService.hasUnreadAdminNotifications(adminNotifications, user.getId()));
      	}
        
    }


    @FXML
    public void onklickOpenInvoiceSubmissionWindow(MouseEvent event) { //created by AI
    	try {
            // Lade die Upload.fxml Datei
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/InvoiceUpload.fxml"));
            Parent root = fxmlLoader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // Neue Szene erstellen
            
            stage.setTitle("Upload Window");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onClickOpenModifyReimbursementWindow(MouseEvent mouseEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/ModifyReimbursement.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

            stage.setTitle("Modify Reimbursement");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    void openCurrentReimbursement(MouseEvent event) {
    	try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/currReimbursements.fxml"));
            Parent root = fxmlLoader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("aktuelle Rechnungen");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @FXML
    void openReimbursementHistory(MouseEvent event) {
    	try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/ReimbursementHistory.fxml"));
            Parent root = fxmlLoader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("alle Rechnungen");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openModifyUsers(MouseEvent mouseEvent) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/ModifyUsers.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

            stage.setTitle("Modify Users");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void openSearch(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/Search.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("Search");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    @FXML
    public void openStatistics(MouseEvent event) {
    	 try {
             FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/Statistics.fxml"));
             Parent root = fxmlLoader.load();

             Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

             stage.setTitle("Search");
             stage.setScene(new Scene(root));
             stage.show();

         } catch (IOException e) {
             e.printStackTrace();
         }
    }
    
    @FXML public void openNotifications(MouseEvent event) {
    	try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/Notification.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("Search");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
