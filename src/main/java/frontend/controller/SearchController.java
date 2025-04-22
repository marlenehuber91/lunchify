package gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import backend.logic.SearchService;

public class SearchController {

    @FXML
    private ListView<String> emailListView;
    @FXML
    private TextField emailSearchField;

    private ObservableList<String> emailList = FXCollections.observableArrayList();

    private final SearchService searchService = new SearchService();

    @FXML
    private void initialize() {
        emailSearchField.textProperty().addListener((observable, oldValue, newValue) -> {
            loadEmailAddresses(newValue);
        });
    }

    private void loadEmailAddresses(String query) {
        emailList.clear();
        emailList.addAll(searchService.searchEmails(query));

        emailListView.setItems(emailList);
    }

    @FXML
    void handleBackToDashboard() {

    }

    @FXML
    void startSearchManually() {

    }
}
