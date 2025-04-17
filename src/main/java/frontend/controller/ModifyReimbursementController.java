package frontend.controller;

import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.model.InvoiceCategory;
import backend.model.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ModifyReimbursementController {

    @FXML
    public Button saveButton;
    @FXML
    public TextField currentAmountField;
    @FXML
    private Text previewText;
    @FXML
    private ComboBox<InvoiceCategory> categoryBox;
    @FXML
    private TextField newAmountField;
    @FXML
    private Label amountLabel;

    
    private User user;
    private ReimbursementService reimbursementService;

    @FXML
    public void initialize() {
    	if(user == null) {
        	user = SessionManager.getCurrentUser();
    	}

    	reimbursementService = new ReimbursementService();

        categoryBox.getItems().addAll(InvoiceCategory.values());
        
        saveButton.setDisable(true);
        
        newAmountField.textProperty().addListener((obs, oldVal, newVal) -> {
            boolean isAmountValid = reimbursementService.isValidFloat(newVal);
        	updateLabel(amountLabel, isAmountValid, "Kein gültiger Zahlenwert", "Betrag eingegeben");
        	checkFields();
        });

        categoryBox.valueProperty().addListener((obs, oldVal, newVal) -> checkFields());
    }

   private void showAlert(String title, String content) {
       	Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
   
   private void checkFields() {
	   String amountText = newAmountField.getText().trim();
	   boolean isAmountValid = reimbursementService.isAmountValid(amountText);
       boolean isCategorySelected = categoryBox.getValue() != null;
       
       if (isCategorySelected) {
           float currentAmount = reimbursementService.getLimit(categoryBox.getValue());
    	   currentAmountField.setText(String.valueOf(currentAmount));
       }
       
       boolean allFieldsFilled = isAmountValid && isCategorySelected;
       saveButton.setDisable(!allFieldsFilled);
     }

    @FXML
    public void onMouseClickedSaveModifyReimbursementButton(MouseEvent mouseEvent) {
        InvoiceCategory category = categoryBox.getValue();
        float currentAmount = Float.parseFloat(currentAmountField.getText().trim());
        float newAmount = Float.parseFloat(newAmountField.getText().trim());

        if (currentAmount == newAmount) {
            showAlert("Betrag identisch", "Der neu gewählte Betrag für die Rückerstattung ist gleich dem alten.");
            saveButton.setDisable(true);
        } else {

            boolean success = reimbursementService.modifyLimits(category, newAmount);
                if (success) {
                    showAlert("Erfolg", "Neuer Rückerstattungsbetrag wurde gespeichert!" + "\n"  + " Kategorie: " + category + "; neuer Rückerstattungsbetrag: " + reimbursementService.getReimbursementAmount());
                    resetForm();
                } else {
                    showAlert("Fehler", "Beim Speichern des neuen Betrages ist ein Fehler aufgetreten.");
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

    private void resetForm() {
        categoryBox.getSelectionModel().clearSelection();
        currentAmountField.clear();
        newAmountField.clear();
        previewText.setText("");
        saveButton.setDisable(true);
        saveButton.setStyle("");
        amountLabel.setStyle("");

        reimbursementService = new ReimbursementService();
    }
    
    private void setReimbursementAmount (String amountText) {
    	float invoiceAmount = Float.parseFloat(amountText);
		float limit = reimbursementService.getLimit(categoryBox.getValue());
		float reimbursementAmount = categoryBox.getValue().calculateReimbursement(invoiceAmount, limit);
		currentAmountField.setText(String.valueOf(reimbursementAmount));
		reimbursementService.setReimbursementAmount(reimbursementAmount);
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
}
