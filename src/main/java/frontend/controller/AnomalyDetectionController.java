package frontend.controller;

import backend.logic.AnomalyDetectionService;
import backend.logic.ReimbursementService;
import backend.model.Anomaly;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.UserRole;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.TableCell;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

import static backend.logic.ReimbursementService.getReimbursementByInvoiceId;

public class AnomalyDetectionController extends ReimbursementHistoryController {

    @FXML private TableView<Anomaly> anomalyTable;
    @FXML private TableColumn<Anomaly, Integer> userId;
    @FXML private TableColumn<Anomaly, String> userName;
    @FXML private TableColumn<Anomaly, Integer> invoiceID;
    @FXML private TableColumn<Anomaly, String> invoiceDate;
    @FXML private TableColumn<Anomaly, Void> editColumn;
    @FXML private TableColumn<Anomaly, Void> deleteColumn;
    @FXML private TableColumn<Anomaly, Void> approveColumn;
    @FXML private TableColumn<Anomaly, Void> rejectColumn;
    @FXML
    private TableColumn<Anomaly, String> status;

    private final ReimbursementService reimbursementService = new ReimbursementService();
    private final AnomalyDetectionService anomalyService = new AnomalyDetectionService();
    private Reimbursement anomalReimbursement = null;
    private Anomaly anomaly = null;


    @FXML
    public void initialize() {
        userId.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getId()).asObject());
        userName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getUserName()));
        invoiceDate.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceDate()));
        invoiceID.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue().getInvoiceId()));

        List<Anomaly> anomalies = anomalyService.extractAnomalies();
        anomalyTable.setItems(FXCollections.observableArrayList(anomalies));

        addEditButton(editColumn);
        addDeleteButton(deleteColumn);
        addApproveButton(approveColumn);
        addRejectButton(rejectColumn);

        // Status-Spalte: Reimbursement-State über invoiceId holen und als String anzeigen
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

            status.setCellFactory(column -> new TableCell<Anomaly, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);

                        if (item.equals("Zur Kontrolle")) {
                            setStyle("-fx-background-color: orange; -fx-text-fill: white;");
                        } else if (item.equals("Genehmigt")) {
                            setStyle("-fx-background-color: green; -fx-text-fill: white;");
                        } else if (item.equals("Abgelehnt")) {
                            setStyle("-fx-background-color: red; -fx-text-fill: white;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            });
            return new SimpleStringProperty(statusText);
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

    private void addImageToColumn(TableColumn<Anomaly, Void> column, String imagePath) {
        column.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(imagePath)));

            {
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
                setGraphic(imageView);
                setOnMouseClicked(event -> {
                    Anomaly anomaly = getTableView().getItems().get(getIndex());
                    System.out.println("Clicked on: " + anomaly); // hier kannst du Aktionen verknüpfen
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(imageView);
                }
            }
        });
    }

    private void addEditButton(TableColumn<Anomaly, Void> column) {
        column.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/pen.png")));

            {
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
                setGraphic(imageView);
                setOnMouseClicked(event -> {
                    Anomaly anomaly = getTableView().getItems().get(getIndex());
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/views/EditReimbursement.fxml"));
                        Parent root = loader.load();
                        Stage stage = new Stage();
                        stage.setScene(new Scene(root));
                        stage.setTitle("Bearbeiten");
                        stage.show();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : imageView);
            }
        });
    }

    private void addDeleteButton(TableColumn<Anomaly, Void> column) {
        column.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/delete.png")));

            {
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
                setGraphic(imageView);
                setOnMouseClicked(event -> {
                    Anomaly anomaly = getTableView().getItems().get(getIndex());
                    reimbursementService.deleteReimbursement(getReimbursementByInvoiceId(anomaly.getInvoiceId()));
                    anomalyTable.getItems().remove(anomaly); // optional: direkt aus Tabelle löschen
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : imageView);
            }
        });
    }

    private void addApproveButton(TableColumn<Anomaly, Void> column) {
        column.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/accept.png")));

            {
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
                setGraphic(imageView);
                setOnMouseClicked(event -> {
                    Anomaly anomaly = getTableView().getItems().get(getIndex());
                    Reimbursement reimbursement = reimbursementService.getReimbursementByInvoiceId(anomaly.getInvoiceId());
                    if (reimbursement != null) {
                        reimbursementService.approveReimbursement(reimbursement);
                        reimbursement.getInvoice().setFlag(false);
                        anomalyTable.refresh(); // ggf. Status-Zelle aktualisieren
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : imageView);
            }
        });
    }

    private void addRejectButton(TableColumn<Anomaly, Void> column) {
        column.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/cross.png")));

            {
                imageView.setFitHeight(16);
                imageView.setFitWidth(16);
                setGraphic(imageView);
                setOnMouseClicked(event -> {
                    Anomaly anomaly = getTableView().getItems().get(getIndex());
                    Reimbursement reimbursement = reimbursementService.getReimbursementByInvoiceId(anomaly.getInvoiceId());
                    if (reimbursement != null) {
                        reimbursementService.rejectReimbursement(reimbursement);
                        reimbursement.getInvoice().setFlag(false);
                        anomalyTable.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : imageView);
            }
        });
    }


}
