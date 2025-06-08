package frontend.controller;

import backend.logic.AnomalyDetectionService;
import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.model.*;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.scene.control.TableRow;
import java.io.IOException;
import java.util.List;
import static backend.logic.ReimbursementService.getReimbursementByInvoiceId;

public class AnomalyDetectionController  {

    @FXML private TableView<Anomaly> anomalyTable;
    @FXML private TableColumn<Anomaly, Integer> userId;
    @FXML private TableColumn<Anomaly, String> userName;
    @FXML private TableColumn<Anomaly, Integer> invoiceID;
    @FXML private TableColumn<Anomaly, String> invoiceDate;
    @FXML private TableColumn<Anomaly, Void> editColumn;
    @FXML private TableColumn<Anomaly, Void> deleteColumn;
    @FXML private TableColumn<Anomaly, Void> approveColumn;
    @FXML private TableColumn<Anomaly, Void> rejectColumn;
    @FXML private ComboBox<String> monthFilterBox;
    @FXML private ComboBox<String> yearFilterBox;

    @FXML
    private TableColumn<Anomaly, String> status;

    private final ReimbursementService reimbursementService = new ReimbursementService();
    private final AnomalyDetectionService anomalyService = new AnomalyDetectionService();

    @FXML
    public void initialize() {
        // Column value factories
        userId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getUserId()).asObject());
        userName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserName()));
        invoiceDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceDate()));
        invoiceID.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getInvoiceId()));

        List<Anomaly> anomalies = anomalyService.extractAnomalies();
        anomalyTable.setItems(FXCollections.observableArrayList(anomalies));

        addEditButton(editColumn);
        addDeleteButton(deleteColumn);
        addApproveButton(approveColumn);
        addRejectButton(rejectColumn);

        populateBoxes();

        monthFilterBox.setOnAction(event -> applyFilter());
        yearFilterBox.setOnAction(event -> applyFilter());

        // Status column rendering
        status.setCellValueFactory(cellData -> {
            int invoiceId = cellData.getValue().getInvoiceId();
            Reimbursement reimbursement = reimbursementService.getReimbursementByInvoiceId(invoiceId);
            String statusText = "Kein Status";

            if (reimbursement != null && reimbursement.getStatus() != null) {
                statusText = switch (reimbursement.getStatus()) {
                    case APPROVED -> "Genehmigt";
                    case REJECTED -> "Abgelehnt";
                    case FLAGGED -> "Zur Kontrolle";
                    case PENDING -> "Offen";
                };
            }
            return new SimpleStringProperty(statusText);
        });

        status.setCellFactory(column -> new TableCell<Anomaly, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);

                    switch (item) {
                        case "Zur Kontrolle" -> setStyle("-fx-background-color: orange; -fx-text-fill: white;");
                        case "Genehmigt"     -> setStyle("-fx-background-color: green; -fx-text-fill: white;");
                        case "Abgelehnt"     -> setStyle("-fx-background-color: red; -fx-text-fill: white;");
                        case "Offen"         -> setStyle("-fx-background-color: lightgray; -fx-text-fill: black;");
                        default              -> setStyle("");
                    }
                }
            }
        });

        // Row factory for dynamic button updates - AI generated
        anomalyTable.setRowFactory(tv -> new TableRow<Anomaly>() {
            @Override
            protected void updateItem(Anomaly anomaly, boolean empty) {
                super.updateItem(anomaly, empty);
                if (anomaly == null || empty) {
                    setStyle("");
                } else {
                    // Make sure button visibility updates with row state
                    Platform.runLater(() -> updateButtonVisibility(anomaly));
                }
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

    private void addEditButton(TableColumn<Anomaly, Void> column) {
        column.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/pen.png")));

            {
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);

                if (empty) {
                    setGraphic(null);
                    setOnMouseClicked(null);
                    return;
                }

                Anomaly anomaly = getTableView().getItems().get(getIndex());
                boolean isOwnInvoice = anomaly.getUserId() == SessionManager.getCurrentUser().getId();

                if (!isOwnInvoice) {
                    setGraphic(imageView);
                    setOnMouseClicked(event -> {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/views/EditReimbursement.fxml"));
                            Parent root = loader.load();

                            EditReimbursementController controller = loader.getController();
                            Reimbursement reimbursement = getReimbursementByInvoiceId(anomaly.getInvoiceId());
                            controller.setOrigin("anomaly");
                            controller.setReimbursement(reimbursement);

                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                            stage.setScene(new Scene(root));
                            stage.setTitle("Bearbeiten");
                            stage.show();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    setGraphic(null);
                    setOnMouseClicked(null);
                }
            }
        });
    }


    private void addDeleteButton(TableColumn<Anomaly, Void> column) {
        column.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/delete.png")));

            {
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Anomaly anomaly = getTableView().getItems().get(getIndex());
                boolean isOwnInvoice = anomaly.getUserId() == SessionManager.getCurrentUser().getId();

                if (!isOwnInvoice) {
                    setGraphic(imageView);
                    setOnMouseClicked(event -> showDeleteAnomalyConfirmationDialog(anomaly));
                } else {
                    setGraphic(null);
                    setOnMouseClicked(null); // Event-Handler entfernen
                }
            }
        });
    }

    private void addApproveButton(TableColumn<Anomaly, Void> column) {
        column.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/accept.png")));

            {
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Anomaly anomaly = getTableView().getItems().get(getIndex());
                boolean isOwnInvoice = anomaly.getUserId() == SessionManager.getCurrentUser().getId();
                Reimbursement reimbursement = reimbursementService.getReimbursementByInvoiceId(anomaly.getInvoiceId());

                if (!isOwnInvoice && (reimbursement == null || reimbursement.getStatus() != ReimbursementState.APPROVED)) {
                    setGraphic(imageView);
                    setOnMouseClicked(event -> showApproveAnomalyConfirmationDialog(anomaly));
                } else {
                    setGraphic(null);
                    setOnMouseClicked(null);
                }
            }
        });
    }


    private void addRejectButton(TableColumn<Anomaly, Void> column) {
        column.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/cross.png")));

            {
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }

                Anomaly anomaly = getTableView().getItems().get(getIndex());
                boolean isOwnInvoice = anomaly.getUserId() == SessionManager.getCurrentUser().getId();
                Reimbursement reimbursement = reimbursementService.getReimbursementByInvoiceId(anomaly.getInvoiceId());

                if (!isOwnInvoice && (reimbursement == null || reimbursement.getStatus() != ReimbursementState.REJECTED)) {
                    setGraphic(imageView);
                    setOnMouseClicked(event -> showRejectAnomalyConfirmationDialog(anomaly));
                } else {
                    setGraphic(null);
                    setOnMouseClicked(null);
                }
            }
        });
    }


    private void populateBoxes() {
        monthFilterBox.setItems(FXCollections.observableArrayList(
                "alle", "Jänner", "Februar", "März", "April", "Mai", "Juni",
                "Juli", "August", "September", "Oktober", "November", "Dezember"));

        yearFilterBox.setItems(FXCollections.observableArrayList("alle", "2024", "2025"));

        monthFilterBox.setValue("alle");
        yearFilterBox.setValue("alle");
    }

    @FXML
    private void applyFilter() { //AI generated
        String selectedMonth = monthFilterBox.getValue();
        String selectedYear = yearFilterBox.getValue();

        List<Anomaly> filtered = anomalyService.extractAnomalies();

        if (!"alle".equals(selectedMonth)) {
            int monthIndex = monthFilterBox.getItems().indexOf(selectedMonth);
            filtered = filtered.stream()
                    .filter(anomaly -> {
                        String[] parts = anomaly.getInvoiceDate().split("-");
                        int anomalyMonth = Integer.parseInt(parts[1]);
                        return anomalyMonth == monthIndex;
                    })
                    .toList();
        }

        if (!"alle".equals(selectedYear)) {
            filtered = filtered.stream()
                    .filter(anomaly -> anomaly.getInvoiceDate().startsWith(selectedYear))
                    .toList();
        }

        anomalyTable.setItems(FXCollections.observableArrayList(filtered));
    }
    private void showDeleteAnomalyConfirmationDialog(Anomaly anomaly) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Bestätigung");
        alert.setHeaderText(null);
        alert.setContentText("Möchten Sie die Rechnung vom " + anomaly.getInvoiceDate() + " wirklich löschen?");

        ButtonType buttonYes = new ButtonType("Ja");
        ButtonType buttonNo = new ButtonType("Nein", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonYes, buttonNo);
        alert.showAndWait().ifPresent(response -> {
            if (response == buttonYes) {
                Reimbursement reimbursement = reimbursementService.getReimbursementByInvoiceId(anomaly.getInvoiceId());
                anomalyService.handleAnomalyDone(anomaly);
                if (reimbursement != null) {
                    User currentUser = SessionManager.getCurrentUser();
                    User invoiceOwner = reimbursement.getInvoice().getUser();
                    boolean selfmade = currentUser.getId() == invoiceOwner.getId();

                    boolean isDeleted = reimbursementService.deleteReimbursement(reimbursement, invoiceOwner, selfmade);

                    if (isDeleted) {
                        anomalyTable.getItems().remove(anomaly);
                    }
                }
            }
        });
    }

    private void showApproveAnomalyConfirmationDialog(Anomaly anomaly) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Bestätigung");
        alert.setHeaderText(null);
        alert.setContentText("Möchten Sie die Rechnung vom " + anomaly.getInvoiceDate() + " wirklich freigeben?");

        ButtonType buttonYes = new ButtonType("Ja");
        ButtonType buttonNo = new ButtonType("Nein", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonYes, buttonNo);
        alert.showAndWait().ifPresent(response -> {
            if (response == buttonYes) {
                Reimbursement reimbursement = reimbursementService.getReimbursementByInvoiceId(anomaly.getInvoiceId());
                if (reimbursement != null) {
                    User currentUser = SessionManager.getCurrentUser();
                    User invoiceOwner = reimbursement.getInvoice().getUser();
                    boolean selfmade = currentUser.getId() == invoiceOwner.getId();

                    boolean isApproved = reimbursementService.approveReimbursement(
                            reimbursement,
                            invoiceOwner,
                            selfmade
                    );

                    if (isApproved) {
                        reimbursement.getInvoice().setFlag(false);
                        anomalyService.handleAnomalyDone(anomaly);
                        anomalyTable.getItems().remove(anomaly);
                    }
                }
            }
        });
    }

    private void showRejectAnomalyConfirmationDialog(Anomaly anomaly) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Bestätigung");
        alert.setHeaderText(null);
        alert.setContentText("Möchten Sie die Rechnung vom " + anomaly.getInvoiceDate() + " wirklich ablehnen?");

        ButtonType buttonYes = new ButtonType("Ja");
        ButtonType buttonNo = new ButtonType("Nein", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonYes, buttonNo);
        alert.showAndWait().ifPresent(response -> {
            if (response == buttonYes) {
                Reimbursement reimbursement = reimbursementService.getReimbursementByInvoiceId(anomaly.getInvoiceId());
                if (reimbursement != null) {
                    User currentUser = SessionManager.getCurrentUser();
                    User invoiceOwner = reimbursement.getInvoice().getUser();
                    boolean selfmade = currentUser.getId() == invoiceOwner.getId();

                    boolean isRejected = reimbursementService.rejectReimbursement(
                            reimbursement,
                            invoiceOwner,
                            selfmade
                    );

                    if (isRejected) {
                        reimbursement.getInvoice().setFlag(false);
                        anomalyService.handleAnomalyDone(anomaly);
                        anomalyTable.getItems().remove(anomaly);
                    }
                }
            }
        });
    }
    private void updateButtonVisibility(Anomaly anomaly) { //AI generated
        Reimbursement reimbursement = reimbursementService.getReimbursementByInvoiceId(anomaly.getInvoiceId());
        if (reimbursement != null) {
            boolean isApproved = reimbursement.getStatus() == ReimbursementState.APPROVED;
            boolean isRejected = reimbursement.getStatus() == ReimbursementState.REJECTED;

            // Find the row in the table
            int index = anomalyTable.getItems().indexOf(anomaly);
            if (index >= 0) {
                // Get the row and update the buttons
                TableRow<Anomaly> row = (TableRow<Anomaly>) anomalyTable.lookup(".table-row-cell[index='" + index + "']");
                if (row != null) {
                    Node approveBtn = row.lookup("#approveColumn");
                    Node rejectBtn = row.lookup("#rejectColumn");

                    if (approveBtn != null) {
                        approveBtn.setVisible(!isApproved);
                    }
                    if (rejectBtn != null) {
                        rejectBtn.setVisible(!isRejected);
                    }
                }
            }
        }
    }


    public void goToFlaggedUsers(MouseEvent event) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/FlaggedUser.fxml"));
            Parent root = fxmlLoader.load();

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setTitle("Auffällige User");
            stage.setScene(new Scene(root));
            stage.show();

        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }
}

