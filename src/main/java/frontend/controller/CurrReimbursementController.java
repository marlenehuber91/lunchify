package frontend.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.logic.UserService;
import backend.model.Reimbursement;
import backend.model.User;
import backend.model.UserRole;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CurrReimbursementController {
	
	User user;
	ReimbursementService reimbursementService;

    @FXML
    private Circle backArrow;

    @FXML
    private StackPane backButton;

    @FXML
    private TableView<Reimbursement> currReimbursementTable;
    

	@FXML
	private TableColumn<Reimbursement, String> invoiceDate;
	
	@FXML
	private TableColumn<Reimbursement, String> invoiceCategory;
	
	@FXML
	private TableColumn<Reimbursement, Float> invoiceAmount;
    
	@FXML
	private TableColumn<Reimbursement, Float> reimbursementAmount;

	@FXML
	private TableColumn<Reimbursement, String> reimbursementState;
	
	@FXML
	private TableColumn<Reimbursement, Integer> reimbursementId;
	
	@FXML
	private Label totalReimbursementAmountLabel;
	
	@FXML
	private Text currentMonthText;
	
	public void setReimbursementService(ReimbursementService reimbursementService) {
		this.reimbursementService = reimbursementService;
	}
	
	@FXML
	void initialize() {
	    if (user == null) {
	        user = SessionManager.getCurrentUser();
	    }

	    setCurrentMonthLabel();
	    if (reimbursementService == null) {
	    	 reimbursementService = new ReimbursementService(user);
	    }
	    loadList();
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
	
	@FXML
	public void handleUserChoice() {
		Reimbursement selectedReimbursement = currReimbursementTable.getSelectionModel().getSelectedItem();
		
		if (selectedReimbursement != null) {

            if (user != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/views/EditReimbursement.fxml"));
                    AnchorPane editReimbursementPane = loader.load();

                    EditReimbursementController controller = loader.getController();
                    controller.setReimbursement(selectedReimbursement);

                    Stage stage = (Stage) currReimbursementTable.getScene().getWindow();
                    stage.setScene(new Scene(editReimbursementPane));

                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
		}   
	}              

    	public void loadList() {
    		List<Reimbursement> reimbursements = reimbursementService.getCurrentReimbursements(user.getId());
    	    String totalReimbursement = String.valueOf(reimbursementService.getTotalReimbursement(reimbursements));
    	    totalReimbursementAmountLabel.setText("€ " + totalReimbursement);
    	    totalReimbursementAmountLabel.setStyle("");
    	    ObservableList<Reimbursement> reimbursementList = FXCollections.observableArrayList(reimbursements);

    	    invoiceDate.setCellValueFactory(
    	            cellData -> new SimpleStringProperty(cellData.getValue().getInvoice().getDate().toString()));

    	    invoiceCategory.setCellValueFactory(
    	            cellData -> new SimpleStringProperty(cellData.getValue().getInvoice().getCategory().toString()));

    	    invoiceAmount.setCellValueFactory(
    	            cellData -> new SimpleFloatProperty(cellData.getValue().getInvoice().getAmount()).asObject());

    	    reimbursementAmount.setCellValueFactory(
    	            cellData -> new SimpleFloatProperty(cellData.getValue().getApprovedAmount()).asObject());

    	    reimbursementState.setCellValueFactory(
    	            cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));
    	    
    	    reimbursementState.setCellFactory(column -> new TableCell<>() { //created by AI
    	        @Override
    	        protected void updateItem(String item, boolean empty) {
    	            super.updateItem(item, empty);

    	            if (empty || item == null) {
    	                setText(null);
    	                setStyle("");
    	            } else {
    	                if (item.equals("PENDING")) {
    	                    setText("offen");
    	                    setStyle("-fx-background-color: yellow; -fx-text-fill: black;");
    	                } if (item.equals("FLAGGED")) {
    	                	setText("in Prüfung durch einen Admin");
    	                    setStyle("-fx-background-color: orange; -fx-text-fill: black;");
    	                } if (item.equals("REJECTED")) {
    	                	setText("abgelehnt");
    	                    setStyle("-fx-background-color: red; -fx-text-fill: white;");
    	                } if (item.equals("APPROVED")) {
    	                    setText(item);
    	                    setText("genehmigt");
    	                    setStyle("-fx-background-color: green; -fx-text-fill: white;");
    	                }
    	            }
    	        }
    	    });
    	    
    	    currReimbursementTable.setItems(reimbursementList);
    	}
    	
    	private void setCurrentMonthLabel() {
    		LocalDate currDate = LocalDate.now();
    		Locale germanLocale = new Locale("de", "DE");		
    		String currMonth = currDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
    		currentMonthText.setText("Rechnungen "  + currMonth);
    	}

		public Label getTotalReimbursementAmountLabel() {
			return this.totalReimbursementAmountLabel;
		}
		
		public Object loadCurrentReimbursements() {
			// TODO Auto-generated method stub
			return null;
		}
}


