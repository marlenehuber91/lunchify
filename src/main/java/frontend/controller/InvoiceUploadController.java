package frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.sql.Date;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import javafx.scene.control.TextField;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.InvoiceState;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;

public class InvoiceUploadController {

	private File uploadedFile;
	
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
    private TextField amountField;

    @FXML
    private DatePicker datePicker;
    
    @FXML
    private Label amountLabel, datePickerLabel, imageUploadLabel;
    
    private List<Invoice> invoices;
    
    private User dummyUser;

    @FXML
    public void initialize() {
    	//dummy User until User Story Login is done
    	dummyUser= new User ("dummy", "dummy@lunch.at", "test" , UserRole.ADMIN, UserState.ACTIVE);
    	
    	invoices = dummyUser.viewCurrentReimbursement();
    	
        categoryBox.getItems().addAll(InvoiceCategory.values());
        
        submitButton.setDisable(true);
        
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
        	boolean isAmountValid = isValidFloat(newVal);
        	updateLabel(amountLabel, isAmountValid, "Kein gültiger Zahlenwert", "Betrag eingegeben");
        	checkFields();
        });
        
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
        	boolean isDateValid = (newVal != null) && isWorkday(newVal);
        	updateLabel(datePickerLabel, isDateValid, "Kein gültiger Arbeitstag!", "Datum eingegeben");
        	checkFields();
        });
        
        categoryBox.valueProperty().addListener((obs, oldVal, newVal) -> checkFields());
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
       boolean isAmountValid = amountText!=null && isValidFloat(amountText);
       boolean isDateValid = datePicker.getValue() != null && isWorkday(datePicker.getValue());
       boolean isCategorySelected = categoryBox.getValue() != null;
       boolean isFileUploaded = uploadedFile != null;
       
       boolean allFieldsFilled = isAmountValid && isDateValid && isCategorySelected && isFileUploaded;
       
       submitButton.setDisable(!allFieldsFilled);
       
       if (allFieldsFilled) {
           submitButton.setStyle("-fx-background-color: #42b35b; -fx-text-fill: white;"); // Grün
       } else {
    	   submitButton.setStyle("");
       }
   }
   
   @FXML
   private void addInvoice() { //TODO: setUser
	   LocalDate date = datePicker.getValue();  
       float amount = Float.parseFloat(amountField.getText());
       InvoiceCategory category = categoryBox.getValue();
       //falls Rechnung möglich: uploadInvoice bei User, falls Rechnung nicht möglich --> Alert oder ähnliches
	   Invoice newInvoice = new Invoice(date,amount, category, InvoiceState.PENDING, uploadedFile, dummyUser);
	   if (invoices != null && invoiceDateAlreadyUsed(date)) {
		   showAlert("Ungültiges Datum", "Für das gewählte Datum wurde bereits eine Rechnung eingereicht. Bitte wähle ein anderes Datum.");
		   submitButton.setDisable(true);
	   } else {
		   //TODO: Logik für Insert in die Datenbank //TODO: Nachricht um Rückerstattungsbetrag ergänzen
		   showAlert("Rechnung eingereicht", "Die Rechnung wurde erfolgreich eingereicht! Kategorie: "+ newInvoice.getCategory());
	   }
	   
   }
   
   private boolean isValidFloat(String text) { //created by AI (ChatGPT)
	   return text.matches("^\\d+(\\.\\d+)?$");
   }
   
   private boolean isWorkday (LocalDate date) {
	   DayOfWeek dayOfWeek = date.getDayOfWeek();
	   return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
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
   
   private boolean invoiceDateAlreadyUsed (LocalDate date) {
	   for (Invoice invoice: invoices) {
		   if (invoice.getDate().equals(date)) return true;
	   } 
	   return false;
   }
  
}
