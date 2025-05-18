package frontend.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import backend.logic.NotificationService;
import backend.logic.SessionManager;
import backend.logic.UserService;
import backend.model.Notification;
import backend.model.User;
import backend.model.UserRole;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class NotificationsController {

	private User user;
	
	private List<Notification> notifications, adminNotifications;
	
	@FXML
	private Circle backArrow;

	@FXML
	private StackPane backButton;

	@FXML
	private Text currentMonthText;

	@FXML
	private TableColumn<Notification, Boolean> isRead;

	@FXML
	private TableColumn<Notification, String> message;

	@FXML
	private TableColumn<Notification, String> newVal;
	
	@FXML
	private AnchorPane notificationPane;

	@FXML
	private TableView<Notification> notificationView;

	@FXML
	private TableColumn<Notification, String> oldVal;
	
	@FXML
	private TableColumn<Notification, LocalDate> originalInvoiceDate;
	
	@FXML
	private TableColumn<Notification, String> processedDate;
	
	@FXML
	private TableView<Notification> adminNotificationView;
	
	@FXML
    private Tab adminNotification;

    @FXML
    private TableColumn<Notification, String> adminOldValue;

    @FXML
    private TableColumn<Notification, String> adminProcessedDate;
    
    @FXML
    private TableColumn<Notification, LocalDate> adminInvoiceDate;

    @FXML
    private TableColumn<Notification, Boolean> adminIsRead;

    @FXML
    private TableColumn<Notification, String> adminMessage;

    @FXML
    private TableColumn<Notification, String> adminNewValue;
    
    @FXML
	private TableColumn<Notification, String> userEmailColumn;
    
    @FXML
    private Circle notificationDot;
    
    @FXML
    private Circle adminNotificationDot;

	
	@FXML
	void initialize () {
		notificationDot.setVisible(false);
		adminNotificationDot.setVisible(false);
		 if (user == null) {
		        user = SessionManager.getCurrentUser();
		 }
		 
		 if (user.getRole() != UserRole.ADMIN) {
				adminNotification.setDisable(true);
				adminNotification.setText("");
		}
		 
		notifications = NotificationService.getNotificationsByUser(user);
		adminNotifications = NotificationService.getAdminNotification();

		adminNotificationDot.setVisible(hasNewAdminNotifications());
		notificationDot.setVisible(hasNewUserNotifications());
		 
		loadTable();
		loadAdminTable();
		 
	}
	
	@FXML
	void handleBackToDashboard(MouseEvent event) {
		String role;
		if (user.getRole() == UserRole.ADMIN)
			role = "AdminDashboard";
		else
			role = "UserDashboard";

		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/" + role + ".fxml"));
			Parent root = fxmlLoader.load();

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

			if (stage != null) {
				stage.setScene(new Scene(root));
				stage.setTitle("Dashboard");
				stage.show();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@FXML
	void handleUserChoice(MouseEvent event) {

	}
	
	public void loadTable() {
	    ObservableList<Notification> notificationsList = FXCollections.observableArrayList(notifications);
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	    originalInvoiceDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOriginalInvoiceDate()));
	    processedDate.setCellValueFactory(cellData -> 
	    new SimpleObjectProperty<>(cellData.getValue().getCreatedAt().format(formatter))); // Falls eine Zeichenkette gewünscht ist
	    message.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));
	    oldVal.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOldValue()));
	    newVal.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNewValue()));

	    isRead.setCellFactory(col -> new TableCell<Notification, Boolean>() {
	        private final CheckBox checkBox = new CheckBox();

	        {
	            checkBox.setOnAction(event -> {
	                Notification notification = getTableView().getItems().get(getIndex());
	                boolean newValue = checkBox.isSelected();
	                notification.setRead(newValue);
	             
	                TableRow<Notification> row = getTableRow();
	                if (row != null) {
	                    row.setStyle(newValue ? "" : "-fx-font-weight: bold;");
	                }

	                NotificationService.markNotificationAsRead(notification.getId(), newValue);
	                notificationDot.setVisible(hasNewUserNotifications());
	            });
	        }

	        @Override
	        protected void updateItem(Boolean item, boolean empty) {
	            super.updateItem(item, empty);
	            if (empty) {
	                setGraphic(null);
	            } else {
	                checkBox.setSelected(item != null && item);
	                setGraphic(checkBox);
	            }
	        }
	    });
	    isRead.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isRead()));
	      

	    //bold if !isRead
	    notificationView.setRowFactory(tv -> new TableRow<Notification>() {
	        @Override
	        protected void updateItem(Notification item, boolean empty) {
	            super.updateItem(item, empty);
	            if (item == null || empty) {
	                setStyle("");
	            } else {
	                setStyle(item.isRead() ? "" : "-fx-font-weight: bold;");
	            }
	        }
	    });

	    notificationView.setItems(notificationsList);
	}
	
	public void loadAdminTable() {
	    ObservableList<Notification> notificationsList = FXCollections.observableArrayList(adminNotifications);
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	    adminInvoiceDate.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getOriginalInvoiceDate()));
	    adminProcessedDate.setCellValueFactory(cellData -> 
	    new SimpleObjectProperty<>(cellData.getValue().getCreatedAt().format(formatter))); // Falls eine Zeichenkette gewünscht ist
	    adminMessage.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getMessage()));
	    adminOldValue.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getOldValue()));
	    adminNewValue.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getNewValue()));
	    
	    userEmailColumn.setCellValueFactory(cellData -> {
	        Long userId = cellData.getValue().getUserId();
	        String email = UserService.getUserById(userId.intValue()).getEmail(); // Achtung: ggf. null prüfen
	        return new SimpleStringProperty(email);
	    });

	    adminIsRead.setCellFactory(col -> new TableCell<Notification, Boolean>() {
	        private final CheckBox checkBox = new CheckBox();

	        {
	            checkBox.setOnAction(event -> {
	                Notification notification = getTableView().getItems().get(getIndex());
	                boolean newValue = checkBox.isSelected();
	                notification.setRead(newValue);
	             
	                TableRow<Notification> row = getTableRow();
	                if (row != null) {
	                    row.setStyle(newValue ? "" : "-fx-font-weight: bold;");
	                }

	                NotificationService.markNotificationAsRead(notification.getId(), newValue);
	                adminNotificationDot.setVisible(hasNewAdminNotifications());
	            });
	        }

	        @Override
	        protected void updateItem(Boolean item, boolean empty) {
	            super.updateItem(item, empty);
	            if (empty) {
	                setGraphic(null);
	            } else {
	                checkBox.setSelected(item != null && item);
	                setGraphic(checkBox);
	            }
	        }
	    });
	    adminIsRead.setCellValueFactory(cellData -> new SimpleBooleanProperty(cellData.getValue().isRead()));
	      

	    //bold if !isRead
	    adminNotificationView.setRowFactory(tv -> new TableRow<Notification>() {
	        @Override
	        protected void updateItem(Notification item, boolean empty) {
	            super.updateItem(item, empty);
	            if (item == null || empty) {
	                setStyle("");
	            } else {
	                setStyle(item.isRead() ? "" : "-fx-font-weight: bold;");
	            }
	        }
	    });

	    adminNotificationView.setItems(notificationsList);
	}
	
	private boolean hasNewUserNotifications() {
		return NotificationService.hasUnreadNotifications(notifications, user.getId());
	}
	
	private boolean hasNewAdminNotifications() {
		if (user.getRole() != UserRole.ADMIN) return false;
		return NotificationService.hasUnreadAdminNotifications(adminNotifications, user.getId());
	}
}
