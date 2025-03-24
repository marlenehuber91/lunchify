package lunchify;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application{
	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			AnchorPane root = (AnchorPane)FXMLLoader.load(getClass().getResource("views/Main.fxml"));
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

}
