package frontend.controller;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import backend.logic.*;
import backend.model.*;
import javafx.beans.property.SimpleFloatProperty;
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
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class InvoiceUploadController {

    @FXML
    private StackPane uploadPane;
    @FXML
    private ImageView uploadedImageView;
    @FXML
    private Text uploadText;
    @FXML
    private Text previewText;
    @FXML
    private Button submitButton;
    @FXML
    private ComboBox<InvoiceCategory> categoryBox;
    @FXML
    private TextField amountField, reimbursementAmountField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Label amountLabel, datePickerLabel, imageUploadLabel;
    @FXML
    private TextArea infoText;

    private List<Invoice> invoices;
    
    private User user;
    private File uploadedFile;
    private InvoiceService invoiceService = new InvoiceService(); 
    private ReimbursementService reimbursementService;
    private Invoice extractedInvoice;

    @FXML
    public void initialize() {
        if (user == null) {
            user = SessionManager.getCurrentUser();
        }

        invoiceService = new InvoiceService(user);
        invoices = invoiceService.getInvoices();
        reimbursementService = new ReimbursementService(user);

        categoryBox.getItems().addAll(InvoiceCategory.values());

        submitButton.setDisable(true);

        infoText.setText(reimbursementService.getInfoText());

        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAmountValid = invoiceService.isValidFloat(newVal);
            updateLabel(amountLabel, isAmountValid, "Kein gültiger Zahlenwert", "Betrag eingegeben");

            checkFields();
        });


        categoryBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            checkFields();
        });


        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);

                LocalDate today = LocalDate.now();
                LocalDate firstDayOfMonth = today.withDayOfMonth(1);
                LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());

                boolean isDisabled = date.isBefore(firstDayOfMonth) || date.isAfter(today);

                setDisable(isDisabled);
                if (isDisabled) {
                    setStyle("-fx-background-color: #d3d3d3;");
                }
            }
        });

        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {

            if (newVal == null) {
                updateLabel(datePickerLabel, false, "Kein Datum ausgewählt!", "");
            } else if (newVal.isBefore(LocalDate.now().withDayOfMonth(1))) {
                updateLabel(datePickerLabel, false, "Datum darf nicht vor dem aktuellen Monat liegen", "");
            } else if (newVal.isAfter(LocalDate.now())) {
                updateLabel(datePickerLabel, false, "Datum darf nicht in der Zukunft liegen", "");
            } else if (!invoiceService.isWorkday(newVal)) {
                updateLabel(datePickerLabel, false, "Kein gültiger Arbeitstag!", "");
            } else {
                updateLabel(datePickerLabel, true, "", "Datum eingegeben");
            }

            checkFields();
        });
    }



    @FXML
    private void openFileChooser() {
    	
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

   private void showAlert(String title, String content) {
       	Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
   
   private void checkFields() {
	   String amountText = amountField.getText().trim();
	   LocalDate date= datePicker.getValue();
	   boolean isValidDate = isDateValid(date);
	   boolean isAmountValid = invoiceService.isamaountValid(amountText);
       boolean isCategorySelected = categoryBox.getValue() != null;
       boolean isFileUploaded = uploadedFile != null;
       
       if (isCategorySelected && isAmountValid) {
    	   setReimbursementAmount(amountText);
		}
       
       boolean allFieldsFilled = isAmountValid && isValidDate && isCategorySelected && isFileUploaded;
       submitButton.setDisable(!allFieldsFilled);
       
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


  
   private void updateLabel(Label label, boolean isValid, String errorText, String successText) {
	   if (!isValid) {
		   label.setText(errorText);
		   label.setStyle("-fx-text-fill: red;");
	   } else {
		   label.setText(successText);
		   label.setStyle("-fx-text-fill: green");
	   }
   }
   
   public File getFile() {
	   return this.uploadedFile;
   }
   
   private boolean isDateValid(LocalDate date) {
	   return (date!=null && !date.isBefore(LocalDate.now().withDayOfMonth(1)) && !date.isAfter(LocalDate.now()) && invoiceService.isWorkday(date));
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
    
    private void setReimbursementAmount (String amountText) {
    	float invoiceAmount = Float.parseFloat(amountText);
		float limit = reimbursementService.getLimit(categoryBox.getValue());
		float reimbursementAmount = categoryBox.getValue().calculateReimbursement(invoiceAmount, limit);
		reimbursementAmountField.setText(String.valueOf(reimbursementAmount));
		reimbursementService.setReimbursementAmount(reimbursementAmount);
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
