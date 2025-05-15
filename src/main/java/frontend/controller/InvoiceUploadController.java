package frontend.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;

import backend.logic.*;
import backend.model.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class InvoiceUploadController extends BaseUploadController {

    private Invoice extractedInvoice;

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
            String filePath = file.getAbsolutePath();
            System.out.println("Datei ausgewählt: " + filePath);
            
            if (filePath.endsWith(".jpg") || filePath.endsWith(".jpeg") || filePath.endsWith(".png")) {
                uploadedImageView.setImage(new Image(file.toURI().toString()));
                uploadedFile = file;
                previewText.setText("Vorschau");
            } else {
            	uploadedFile = file;
                showAlert("Datei hochgeladen", "Die Datei wurde erfolgreich ausgewählt:\n" + filePath);
            }

            try {
                OCRService ocrService = new OCRService();
                extractedInvoice = ocrService.extractData(file);

                if (extractedInvoice != null) {
                    amountField.setText(String.valueOf(extractedInvoice.getAmount()));
                    datePicker.setValue(extractedInvoice.getDate());
                    categoryBox.setValue(extractedInvoice.getCategory());

                    OCR.setAmount(extractedInvoice.getAmount());
                    OCR.setDate(extractedInvoice.getDate());
                    OCR.setCategory(extractedInvoice.getCategory());
                }
                extractedInvoice.setFlag(false); //intitially false, only true if user alters amount manually

            } catch (Exception e) {
                showAlert("Fehler", "Die Datei konnte nicht analysiert werden.");
                e.printStackTrace();
            }

            uploadText.setText("Foto hochgeladen");

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
            showAlert("Ungültiges Datum", "Für das gewählte Datum wurde bereits eine Rechnung eingereicht. Bitte wähle ein anderes Datum.");
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
