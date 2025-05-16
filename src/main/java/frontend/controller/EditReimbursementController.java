package frontend.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;

import backend.logic.SessionManager;
import backend.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import static backend.logic.ReimbursementService.getReimbursementByInvoiceId;

public class EditReimbursementController extends BaseUploadController{
	
	protected Reimbursement reimbursement;
	protected Invoice selectedInvoice;
	protected User user;
	protected User selectedUser;
	private boolean selfmade;
	private String origin;
    
    public void setReimbursement (Reimbursement reimb) {
    	this.reimbursement = reimb;
    	loadData();
    }
    
    @FXML
    public void initialize() {
    	super.initialize();
        categoryBox.setItems(FXCollections.observableArrayList(InvoiceCategory.values()));
        
        if (user == null) {
			user = SessionManager.getCurrentUser();
		}
    }
    
    
   @FXML
   private void showEditConfirmationDialog() {
	   if ((selectedUser == null) ||  (user.getId() == selectedUser.getId())) {
		   selfmade = true;
	   }
	   
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
    		   System.out.println("EditReimb: showEdiCon + if" + selfmade );
    		   boolean isReimbAltered = reimbursementService.updateReimbursementIfChanged(reimbursement, newReimb, selectedInvoice.getUser(), selfmade);
    		   boolean isInvoiceAltered =  invoiceService.updateInvoiceIfChanged(reimbursement.getInvoice(), newReimb.getInvoice(), reimbursement.getInvoice().getUser(), selfmade);
    		   
    		   if (isReimbAltered || isInvoiceAltered) {
    	    	   showAlert("Erfolg", "Änderungen wurden gespeichert");
    	    	   handleBack();
    	       }
    	   }
       });
   }

   @FXML
   private void showDeleteConfirmationDialog() {
	   Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
	   alert.setTitle("Bestätigung");
	   alert.setHeaderText(null);
	   alert.setContentText("Wollen Sie diesen Rückerstattungsantrag löschen?");

	   ButtonType buttonSave = new ButtonType("Ja");
	   ButtonType buttonCancel = new ButtonType("Nein", ButtonBar.ButtonData.CANCEL_CLOSE);

	   alert.getButtonTypes().setAll(buttonSave, buttonCancel);
	   alert.showAndWait().ifPresent(response -> {
		   if (response == buttonSave) {
			   Reimbursement toDeleteReimb = reimbursement;
			   boolean isReimbDeleted = reimbursementService.deleteReimbursement(toDeleteReimb, toDeleteReimb.getInvoice().getUser(), false);

			   if (isReimbDeleted) {
				   showAlert("Erfolg", "Rückerstattungsantrag gelöscht.");
				   handleBack();
			   }
		   }
	   });
   }

   public File getFile() {
	   return this.uploadedFile;
   }
   
   public User getSelectedUser() {
	   return this.selectedUser;
   }
   
   public void setSelectedUser(User selectedUser) {
	   this.selectedUser = selectedUser;
   }
   
   private boolean isDateValid(LocalDate date) {
	   return (date!=null && !date.isBefore(LocalDate.now().withDayOfMonth(1)) && !date.isAfter(LocalDate.now()) && invoiceService.isWorkday(date));
   }
   
   protected void openFileChooser() {
	   super.openFileChooser();
	   submitButton.setDisable(false);
   }

   public void handleBack() {
		handleBackToCurrReimb();
   }


    @FXML
    private void handleBackToCurrReimb() {
    	try {
			Parent root = null;
            if ("anomaly".equals(origin)) {
				origin = null;
				FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/AnomalyDetection.fxml"));
				root = fxmlLoader.load();
			} else {
				FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/currReimbursements.fxml"));
				root = fxmlLoader.load();
			}

            Stage stage = (Stage) uploadPane.getScene().getWindow();

            stage.setTitle("aktuelle Rechnungen");
            stage.setScene(new Scene(root));
            stage.show();
        } catch(NullPointerException e) {
        	
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
		newInvoice.setFlag(true);

		Reimbursement newReimb = new Reimbursement(newInvoice, newReimbursementAmount, Date.valueOf(LocalDate.now()), ReimbursementState.FLAGGED);
		newReimb.setId(reimbursement.getId());
		return newReimb;
	}

	public void setOrigin(String origin) {
		this.origin = origin;
	}

}
