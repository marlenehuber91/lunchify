package frontend.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import backend.logic.*;
import backend.model.*;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class InvoiceUploadController extends BaseUploadController {

    private Invoice extractedInvoice;
    @FXML
    private ProgressIndicator loadingIndicator;


    @FXML
    public void initialize() {
        super.initialize();
    }

    @FXML
    protected void openFileChooser() {
        Stage stage = (Stage) uploadPane.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Bild oder PDF hochladen");

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Bilddateien und PDF", "*.jpg", "*.jpeg", "*.png", "*.pdf"));

        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            loadingIndicator.setVisible(true); // Zeige Indikator sofort

            String filePath = file.getAbsolutePath();
            Task<Void> task = new Task<>() {
                @Override
                protected Void call() {
                    try {
                        if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith(".png")) {
                            Platform.runLater(() -> {
                                uploadedImageView.setImage(new Image(file.toURI().toString()));
                                previewText.setText("Vorschau");
                            });
                        }

                        uploadedFile = file;

                        OCRService ocrService = new OCRService();
                        extractedInvoice = ocrService.extractData(file);

                        if (extractedInvoice != null) {
                            OCR.setAmount(extractedInvoice.getAmount());
                            OCR.setDate(extractedInvoice.getDate());
                            OCR.setCategory(extractedInvoice.getCategory());

                            Platform.runLater(() -> {
                                amountField.setText(String.valueOf(extractedInvoice.getAmount()));
                                datePicker.setValue(extractedInvoice.getDate());
                                categoryBox.setValue(extractedInvoice.getCategory());
                            });
                        }

                        extractedInvoice.setFlag(false);

                    } catch (Exception e) {
                        Platform.runLater(() -> showAlert("Fehler", "Die Datei konnte nicht analysiert werden."));
                        e.printStackTrace();
                    }

                    return null;
                }

                @Override
                protected void succeeded() {
                    loadingIndicator.setVisible(false);
                    uploadText.setText("Foto hochgeladen");
                }

                @Override
                protected void failed() {
                    loadingIndicator.setVisible(false);
                    showAlert("Fehler", "Es ist ein Fehler aufgetreten.");
                }
            };
            task.setOnFailed(e -> {
                loadingIndicator.setVisible(false);
                showAlert("Fehler", "Die Datei konnte nicht analysiert werden");
                if (task.getException() != null) {
                    task.getException().printStackTrace();
                }
            });
            new Thread(task).start(); // Starte im Hintergrund

        } else {
            showAlert("Keine Datei", "Es wurde keine Datei ausgewählt.");
        }
    }



    @FXML
    private void addInvoice() {
        LocalDate date = datePicker.getValue();
        InvoiceCategory category = categoryBox.getValue();
        float amount = Float.parseFloat(amountField.getText().trim());

        if (invoices != null && invoiceService.invoiceDateAlreadyUsed(date, user)) {
            showAlert("Ungültiges Datum", "Für das gewählte Datum wurde bereits eine Rechnung eingereicht. Es ist genau eine Rechnung pro Arbeitstag erlaubt.");
            submitButton.setDisable(true);
        } else {
            Invoice invoice = new Invoice();
            invoice.setDate(date);
            invoice.setAmount(amount);
            invoice.setCategory(category);
            invoice.setUser(user);
            invoice.setFile(uploadedFile);

            boolean success = invoiceService.addInvoice(invoice) && reimbursementService.addReimbursement(invoice, reimbursementService.getReimbursementAmount());

            if (success) {
                invoices.add(invoice);
                showAlert("Erfolg", "Rechnung wurde erfolgreich hinzugefügt!" + "\n"  + " Kategorie: " + category + "; Rechnungsbetrag: " + reimbursementService.getReimbursementAmount());
                resetForm();
            } else {
                showAlert("Fehler", "Beim Speichern der Rechnung ist ein Fehler aufgetreten.");
            }
       }
    }

    private void resetForm() {
        datePicker.setValue(null);
        categoryBox.getSelectionModel().clearSelection();
        amountField.clear();
        uploadedFile = null;
        uploadedImageView.setImage(null);
        previewText.setText("");
        uploadText.setText("Kein Foto ausgewählt");
        submitButton.setDisable(true);
        submitButton.setStyle("");
        amountLabel.setStyle("");
        datePickerLabel.setStyle("");
        if (imageUploadLabel!=null) imageUploadLabel.setStyle("");
        reimbursementAmountField.setText("");
    }
    
    @FXML
    private void handleBackToDashboard(MouseEvent event) {
    	String role;
    	if (user.getRole() == UserRole.ADMIN) role = "AdminDashboard";
    	else role="UserDashboard";
    	
    	try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/" + role + ".fxml"));
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
