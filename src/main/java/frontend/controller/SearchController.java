package frontend.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

public class SearchController {

    @FXML
    private Label userNotFoundLabel;

    @FXML
    private ListView<String> listOfMatches;

    @FXML
    private TextField searchField;

    @FXML
    private ImageView searchButton;

    @FXML
    public void initialize() {
        // Setze die ListView und Label initial auf unsichtbar
        listOfMatches.setVisible(false);
        userNotFoundLabel.setVisible(false);
    }

    //TODO ALLES HIER IST NOCH DUMMY CODE ZUM TESTEN!! DAS MUSS NOCH GEÄNDERT WERDEN!

    @FXML
    private void startSearchManually(MouseEvent event) {
        String searchQuery = searchField.getText().trim();
        if (searchQuery.isEmpty()) {
            // Wenn das Suchfeld leer ist, zeige keine Ergebnisse an
            listOfMatches.setVisible(false);
            userNotFoundLabel.setVisible(false);
        } else {
            // TODO: Führe hier eine Datenbankabfrage durch, um passende E-Mail-Adressen zu finden

            // Beispiel für das Hinzufügen von Daten zu ListView
            listOfMatches.getItems().clear(); // Leere Liste bevor neue Daten hinzugefügt werden
            listOfMatches.getItems().add("alexander@example.com");
            listOfMatches.getItems().add("andrea@example.com");
            listOfMatches.getItems().add("alessio@example.com");

            // Wenn keine Ergebnisse gefunden wurden
            if (listOfMatches.getItems().isEmpty()) {
                userNotFoundLabel.setVisible(true);
            } else {
                userNotFoundLabel.setVisible(false);
                listOfMatches.setVisible(true);
            }
        }
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
}
