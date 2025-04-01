package frontend.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.sql.Date;
import java.time.LocalDate;

import javafx.scene.control.TextField;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.InvoiceState;

public class InvoiceUploadController {

	File uploadedFile;

	
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
    public void initialize() {
        categoryBox.getItems().addAll(InvoiceCategory.values());
        
        submitButton.setDisable(true);
        
        amountField.textProperty().addListener((observable, oldValue, newValue) -> checkFields());
        datePicker.valueProperty().addListener((obs, oldVal, newVal) -> checkFields());
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
	   // TODO: add float/int check for field amount
       boolean isAmountFilled = !amountField.getText().trim().isEmpty();
       boolean isDateSelected = datePicker.getValue() != null;
       boolean isCategorySelected = categoryBox.getValue() != null;
       boolean isFileUploaded = uploadedImageView.getImage() != null;
       
       boolean allFieldsFilled = isAmountFilled && isDateSelected && isCategorySelected && isFileUploaded;
       
       submitButton.setDisable(!allFieldsFilled);

       if (allFieldsFilled) {
           submitButton.setStyle("-fx-background-color: #42b35b; -fx-text-fill: white;"); // Grün
       }
       
       submitButton.setDisable(!(isAmountFilled && isDateSelected && isCategorySelected && isFileUploaded));
   }
   
   @FXML
   private void addInvoice() { //TODO: setUser
	   LocalDate date = datePicker.getValue();  
       float amount = Float.parseFloat(amountField.getText());
       InvoiceCategory category = categoryBox.getValue();
	   Invoice invoice = new Invoice(date,amount, category, InvoiceState.PENDING, uploadedFile);
	   
	   System.out.println(invoice.getAmount());
   }
}
