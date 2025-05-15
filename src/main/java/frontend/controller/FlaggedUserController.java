package frontend.controller;

// FlaggedUserController.java

import backend.model.FlaggedUser;
import backend.logic.FlaggedUserService;
import backend.logic.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Callback;

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

        loadFlaggedUsers();
        addEditButtonToTable();
    }

    private void loadFlaggedUsers() {
        List<FlaggedUser> userList = flaggedUserService.getAllFlaggedUsers();
        ObservableList<FlaggedUser> observableList = FXCollections.observableArrayList(userList);
        anomalyTable.setItems(observableList);
    }

    private void addEditButtonToTable() {
        Callback<TableColumn<FlaggedUser, Void>, TableCell<FlaggedUser, Void>> cellFactory = param -> new TableCell<>() {
            private final ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/edit.png")));

            {
                editIcon.setFitHeight(20);
                editIcon.setFitWidth(20);
                editIcon.setOnMouseClicked(event -> {
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
                    setGraphic(editIcon);
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
                flaggedUserService.removePermanentFlag(user.getUserId());
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
}

