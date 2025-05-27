package frontend.controller;

// FlaggedUserController.java

import backend.model.FlaggedUser;
import backend.exceptions.InfrastructureException;
import backend.logic.FlaggedUserService;
import backend.logic.SessionManager;
import backend.model.Reimbursement;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class FlaggedUserController {

    @FXML
    private TableView<FlaggedUser> anomalyTable;

    @FXML
    private TableColumn<FlaggedUser, Integer> userId;

    @FXML
    private TableColumn<FlaggedUser, String> userName;

    @FXML
    private TableColumn<FlaggedUser, Boolean> permFlagStatus;

    @FXML
    private TableColumn<FlaggedUser, Void> removePermFlag;

    private final FlaggedUserService flaggedUserService = new FlaggedUserService();

    @FXML
    public void initialize() {
        userId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        userName.setCellValueFactory(new PropertyValueFactory<>("userName"));
        permFlagStatus.setCellValueFactory(new PropertyValueFactory<>("permanentFlag"));

        permFlagStatus.setCellFactory(column -> new TableCell<FlaggedUser, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "ja" : "nein");
                }
            }
        });

        loadFlaggedUsers();
        addEditButtonToTable();
    }

    private void loadFlaggedUsers() {
        List<FlaggedUser> userList = flaggedUserService.getFlaggedUsers();
        ObservableList<FlaggedUser> observableList = FXCollections.observableArrayList(userList);
        anomalyTable.setItems(observableList);
    }

    private void addEditButtonToTable() {
        Callback<TableColumn<FlaggedUser, Void>, TableCell<FlaggedUser, Void>> cellFactory = tableColumn -> new TableCell<>() {

            private final Button editButton = new Button();

            {
                ImageView icon = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/pen.png")));
                icon.setFitHeight(16);
                icon.setFitWidth(16);
                icon.setPreserveRatio(true);

                editButton.setGraphic(icon);
                editButton.setStyle("-fx-background-color: transparent; -fx-cursor: hand;");

                editButton.setOnAction(e -> {
                    FlaggedUser user = getTableView().getItems().get(getIndex());
                    handleRemovePermFlag(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    FlaggedUser user = getTableView().getItems().get(getIndex());
                    if (user.isPermanentFlag()) {
                        setGraphic(editButton);
                    } else {
                        setGraphic(null);
                    }
                }
            }
        };

        removePermFlag.setCellFactory(cellFactory);
    }



    private void handleRemovePermFlag(FlaggedUser user) {
        int currentUserId = SessionManager.getCurrentUser().getId();

        if (user.getUserId() == currentUserId) {
            showAlert("Warnung", "Eigene Flags können nicht selbst entfernt werden!", Alert.AlertType.WARNING);
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION, "Wollen Sie die Permanent Flag wirklich entfernen?", ButtonType.YES, ButtonType.NO);
        confirmation.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    flaggedUserService.removePermanentFlag(user.getUserId());
                } catch (SQLException e) {
                    throw new InfrastructureException("Fehler beim Entfernen der Permanenten Flag in der Datenbank");
                }
                loadFlaggedUsers();
            }
        });
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleBackToAnomaly(MouseEvent event) {

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/AnomalyDetection.fxml"));
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

