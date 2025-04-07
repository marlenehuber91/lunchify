package frontend;

import backend.Exceptions.AuthenticationException;
import backend.logic.SessionManager;
import backend.logic.UserService;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class Main extends Application {
	//set true for testing
	private boolean isOnTestMode = false; 
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			AnchorPane root;
			if (isOnTestMode) {
				setTestUserSession();
				root = (AnchorPane)FXMLLoader.load(getClass().getResource("/frontend/views/AdminDashboard.fxml"));
			}
			else {
				root = (AnchorPane)FXMLLoader.load(getClass().getResource("/frontend/views/LoginPage.fxml"));
			}
			Scene scene = new Scene(root, 1280, 832);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	private void setTestUserSession() {
		User testUser = new User("dummyName", "martin.lechner@lunch.at", "martin123", UserRole.ADMIN, UserState.ACTIVE);
		try {
			SessionManager.setCurrentUser(UserService.authenticate(testUser.getEmail(), testUser.getPassword()));
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}
	}
	
	public void setTestMode(boolean testMode) {
		isOnTestMode = testMode;
	}
}
