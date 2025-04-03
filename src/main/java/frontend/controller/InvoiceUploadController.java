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
import backend.logic.InvoiceService;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.InvoiceState;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;

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
    private TextField amountField;

    @FXML
    private DatePicker datePicker;
    
    @FXML
    private Label amountLabel, datePickerLabel, imageUploadLabel;
    
    private List<Invoice> invoices;
    
    private User user;
    private File uploadedFile;
    private InvoiceService invoiceService = new InvoiceService(); 

    @FXML
    public void initialize() {
    	//dummy User until User Story Login is done
    	user= new User ("dummy", "dummy@lunch.at", "test" , UserRole.ADMIN, UserState.ACTIVE);
    	
    	invoiceService = new InvoiceService(user);
    	invoices=invoiceService.getInvoices();
    	
        categoryBox.getItems().addAll(InvoiceCategory.values());
        
        submitButton.setDisable(true);
        
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
        	boolean isAmountValid = invoiceService.isValidFloat(newVal);
        	updateLabel(amountLabel, isAmountValid, "Kein gültiger Zahlenwert", "Betrag eingegeben");
        	checkFields();
        });
        
        datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() { //created by AI (ChatGPT
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
	   boolean isAmountValid = invoiceService.isamaountValid(amountText);
       boolean isDateValid = datePicker.getValue() != null && invoiceService.isWorkday(datePicker.getValue());
       //updateLabel(datePickerLabel, isDateValid,"Bitte wähle ein richtiges Datum", "Datum ausgewählt");
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
   private void addInvoice() { // not finished yet
	   //TODO: setUser
	   LocalDate date = datePicker.getValue();  
       InvoiceCategory category = categoryBox.getValue();
       float amount = Float.parseFloat(amountField.getText().trim());
       
	   if (invoices != null && invoiceService.invoiceDateAlreadyUsed(date, user)) {
		   showAlert("Ungültiges Datum", "Für das gewählte Datum wurde bereits eine Rechnung eingereicht. Bitte wähle ein anderes Datum.");
		   submitButton.setDisable(true);
	   } else {
		   
		   if (invoiceService.addInvoice(new Invoice(date, amount, category, InvoiceState.PENDING, uploadedFile, user))) {
			   showAlert("Rechnung eingereicht", "Die Rechnung wurde erfolgreich eingereicht!" + "\n" +" Kategorie: "+ categoryBox.getValue());
		   }
		   else {
			   showAlert("Fehler", "Die Rechnung konnte nicht eingereicht werden. Versuche es erneut.");
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
   
}
