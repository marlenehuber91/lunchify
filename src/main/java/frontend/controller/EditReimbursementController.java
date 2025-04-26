package frontend.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import backend.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class EditReimbursementController  extends BaseUploadController{
	
	private Reimbursement reimbursement;
	private Invoice selectedInvoice;
    
    public void setReimbursement (Reimbursement reimb) {
    	this.reimbursement = reimb;
    	loadData();
    }
    
    @FXML
    public void initialize() {
    	super.initialize();
        categoryBox.setItems(FXCollections.observableArrayList(InvoiceCategory.values()));
    }
    
    
   @FXML
   private void showConfirmationDialog () {
	   
	   Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
       alert.setTitle("Bestätigung");
       alert.setHeaderText(null);
       alert.setContentText("Wollen Sie ihre Änderungen speichern?");
       
       ButtonType buttonSave = new ButtonType("Speichern");
	   ButtonType buttonCancel = new ButtonType("Abbrechen", ButtonBar.ButtonData.CANCEL_CLOSE);
	   
	   alert.getButtonTypes().setAll(buttonSave, buttonCancel);
       alert.showAndWait().ifPresent(response -> {
    	   if (response == buttonSave) {
    		   Reimbursement newReimb = getReimbursement();
    		   boolean isReimbAltered = reimbursementService.updateReimbursementIfChanged(reimbursement, newReimb);
    		   boolean isInvoiceAltered =  invoiceService.updateInvoiceIfChanged(reimbursement.getInvoice(), newReimb.getInvoice());
    		   
    		   if (isReimbAltered || isInvoiceAltered) {
    	    	   showAlert("Erfolg", "Änderungen wurden gespeichert");
    	    	   handleBackToCurrReimb();
    	       }
    	   }
       });
   }
  
   public File getFile() {
	   return this.uploadedFile;
   }
   
   private boolean isDateValid(LocalDate date) {
	   return (date!=null && !date.isBefore(LocalDate.now().withDayOfMonth(1)) && !date.isAfter(LocalDate.now()) && invoiceService.isWorkday(date));
   }
   
   protected void openFileChooser() {
	   super.openFileChooser();
	   submitButton.setDisable(false);
   }
    
    @FXML
    private void handleBackToCurrReimb() {
    	try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/currReimbursements.fxml"));
            Parent root = fxmlLoader.load();
            
            Stage stage = (Stage) uploadPane.getScene().getWindow();

            stage.setTitle("aktuelle Rechnungen");
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void loadData() {
		selectedInvoice = invoiceService.loadInvoice(reimbursement);
		reimbursement.setInvoice(selectedInvoice);
		populateBoxes();
	}
    
	public void populateBoxes() {
		
		if (selectedInvoice.getCategory() != null) {
			categoryBox.setValue(selectedInvoice.getCategory());
		} else {
			categoryBox.getSelectionModel().clearSelection();
		}

		String amount = String.valueOf(selectedInvoice.getAmount());
		amountField.setText(amount);
		datePicker.setValue(selectedInvoice.getDate());
		
		Image uploadedImage = new Image(selectedInvoice.getFile().toURI().toString());
		
		String fileName = selectedInvoice.getFile().getName().toLowerCase();
		if (fileName.endsWith(".pdf")) {
			uploadedImageView.setImage(null);
		}
		else {
			uploadedImageView.setImage(uploadedImage);
		}
		
		uploadedFile = selectedInvoice.getFile();

	}
	public Reimbursement getReimbursement() {
		
		LocalDate newDate = datePicker.getValue();
		InvoiceCategory newCategory = categoryBox.getValue();
		float newAmount = Float.parseFloat(amountField.getText().trim());
		float newReimbursementAmount = Float.parseFloat(reimbursementAmountField.getText().trim());
		Invoice newInvoice = new Invoice(newDate, newAmount, newCategory, uploadedFile, user);
		
		Reimbursement newReimb = new Reimbursement(newInvoice, newReimbursementAmount, Date.valueOf(LocalDate.now()));
		newReimb.setId(reimbursement.getId());
		return newReimb;
	}
}
