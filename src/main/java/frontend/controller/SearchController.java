package frontend.controller;

import backend.logic.SearchService;
import backend.model.User;
import backend.logic.UserService;
import database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class SearchController {

    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> listOfMatches;
    @FXML
    private Label userNotFoundLabel;

    @FXML
    public void initialize() {
        SearchService.setConnectionProvider(DatabaseConnection::connect);

        listOfMatches.setVisible(false);
        userNotFoundLabel.setVisible(false);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                try {
                    List<String> results = new SearchService().searchUsers(newValue);
                    listOfMatches.getItems().setAll(results);

                    if (results.isEmpty()) {
                        listOfMatches.setVisible(false);
                        userNotFoundLabel.setVisible(true);
                    } else {
                        listOfMatches.setVisible(true);
                        userNotFoundLabel.setVisible(false);
                    }

                } catch (Exception e) {
                    listOfMatches.getItems().setAll("Fehler bei der Suche: " + e.getMessage());
                    listOfMatches.setVisible(true);
                    userNotFoundLabel.setVisible(false);
                }
            } else {
                listOfMatches.getItems().clear();
                listOfMatches.setVisible(false);
                userNotFoundLabel.setVisible(false);
            }
        });
    }


    @FXML
    private void handleBackToDashboard(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/AdminDashboard.fxml"));
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
    private void handleUserChoice(MouseEvent event) {
        String selectedEmail = listOfMatches.getSelectionModel().getSelectedItem();

        if (selectedEmail != null) {
            User user = UserService.getUserByEmail(selectedEmail);

            if (user != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/views/ReimbursementHistory.fxml"));
                    AnchorPane historyPane = loader.load();

                    ReimbursementHistoryController controller = loader.getController();
                    controller.loadReimbursementsForUser(user);

                    Stage stage = (Stage) listOfMatches.getScene().getWindow();
                    stage.setScene(new Scene(historyPane));

                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
