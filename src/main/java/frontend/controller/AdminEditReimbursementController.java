package frontend.controller;


import backend.model.ReimbursementState;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AdminEditReimbursementController extends EditReimbursementController {
    @FXML
    private ComboBox<ReimbursementState> statusBox;

    @FXML
    public void initialize() {
        super.initialize();
        statusBox.setItems(FXCollections.observableArrayList());
    }

    @Override
    public void handleBack() {
        handleBackToReimbursementHistoryController();
    }

    @FXML
    private void handleBackToReimbursementHistoryController() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/ReimbursementHistory.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) uploadPane.getScene().getWindow();

            stage.setTitle("alle Rechnungen");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void populateBoxes() {
        if (reimbursement.getStatus() != null)
            statusBox.getSelectionModel().select(reimbursement.getStatus());
        else {
            statusBox.getSelectionModel().clearSelection();
        }
        super.populateBoxes();
    }
}
