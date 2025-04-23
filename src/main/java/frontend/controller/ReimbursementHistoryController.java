package frontend.controller;

import java.io.IOException;
import java.util.List;
import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import backend.model.UserRole;
import javafx.application.Platform;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ReimbursementHistoryController {

	User user;
	ReimbursementService reimbursementService;
	List<Reimbursement> reimbursements;
	String month;
	String year;
	String category;
	String status;
	String totalReimbursement;

	@FXML
	private Circle backArrow;

	@FXML
	private StackPane backButton;

	@FXML
	private TableView<Reimbursement> reimbursementHistoryTable;

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
	private Label totalReimbursementAmountLabel;

	@FXML
	private ComboBox<String> monthFilterBox;

	@FXML
	private ComboBox<String> categoryFilterBox;

	@FXML
	private ComboBox<String> statusFilterBox;

	@FXML
	private ComboBox<String> yearFilterBox;
	
	@FXML
	private Text textTotalReimb;
	
	@FXML
	private Rectangle resetFilterButton;

	@FXML
	void initialize() {
		if (user == null) {
			user = SessionManager.getCurrentUser();
		}
		reimbursementService = new ReimbursementService(user);
		reimbursements = reimbursementService.getAllReimbursements();
		populateBoxes();
		loadList();
		
		//falls immer geupdatet werden soll
		monthFilterBox.setOnAction(e -> handleFilter(null));
	    yearFilterBox.setOnAction(e -> handleFilter(null));
	    categoryFilterBox.setOnAction(e -> handleFilter(null));
	    statusFilterBox.setOnAction(e -> handleFilter(null));
	}

	@FXML
	private void handleBackToDashboard(MouseEvent event) {
		String role;
		if (user.getRole() == UserRole.ADMIN)
			role = "AdminDashboard";
		else
			role = "UserDashboard";

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
	private void handleFilter(MouseEvent event) {
		getFilterInput();
		reimbursements = reimbursementService.getFilteredReimbursements(month, year, category, status);
		loadList();
		textTotalReimb.setText("Summe Rückerstattungen: '" + translateStauts(status)+ "'" );
	}
	
	@FXML
	private void handleResetFilter(MouseEvent event) {
		monthFilterBox.setValue(null);
		yearFilterBox.setValue(null);
		categoryFilterBox.setValue(null);
		statusFilterBox.setValue(null);
	}

	private void loadList() {
		ObservableList<Reimbursement> reimbursementList = FXCollections.observableArrayList(reimbursements);

		invoiceDate.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getInvoice().getDate().toString()));

		invoiceCategory.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getInvoice().getCategory().toString()));

		invoiceAmount.setCellValueFactory(
				cellData -> new SimpleFloatProperty(cellData.getValue().getInvoice().getAmount()).asObject());

		reimbursementAmount.setCellValueFactory(
				cellData -> new SimpleFloatProperty(cellData.getValue().getApprovedAmount()).asObject());

		reimbursementState
				.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStatus().toString()));

		reimbursementState.setCellFactory(column -> new TableCell<>() { // created by AI
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
					}
					if (item.equals("FLAGGED")) {
						setText("in Prüfung durch einen Admin");
						setStyle("-fx-background-color: orange; -fx-text-fill: black;");
					}
					if (item.equals("REJECTED")) {
						setText("abgelehnt");
						setStyle("-fx-background-color: red; -fx-text-fill: white;");
					}
					if (item.equals("APPROVED")) {
						setText("genehmigt");
						setStyle("-fx-background-color: green; -fx-text-fill: white;");
					}
				}
			}
		});

		reimbursementHistoryTable.setItems(reimbursementList);
		calculateTotalReimbursement();
	}

	private void populateBoxes() {
		monthFilterBox.setItems(FXCollections.observableArrayList("alle", "Jänner", "Februar", "März", "April",
				"Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"));
		yearFilterBox.setItems(FXCollections.observableArrayList("2024", "2025", "alle"));
		categoryFilterBox.setItems(FXCollections.observableArrayList("Restaurant", "Supermarkt", "alle"));
		statusFilterBox.setItems(
				FXCollections.observableArrayList("abgelehnt", "genehmigt", "offen", "zur Kontrolle", "alle"));
	}

	public void getFilterInput() {
		month = "alle".equals(monthFilterBox.getValue()) ? null : monthFilterBox.getValue();
		year = "alle".equals(yearFilterBox.getValue()) ? null : yearFilterBox.getValue();
		category = mapCategory(categoryFilterBox.getValue());
		status = mapStatus (statusFilterBox.getValue());
	}
	
	private void calculateTotalReimbursement () {
		if (statusFilterBox.getValue() == null  || statusFilterBox.getValue().equals("alle")) {
		totalReimbursement = String.valueOf(reimbursementService.getTotalReimbursement(reimbursements));
		} else {
			ReimbursementState state = ReimbursementState.getState(statusFilterBox.getValue());
			totalReimbursement = String.valueOf(reimbursementService.getTotalReimbursement(reimbursements, state));
		}
		totalReimbursementAmountLabel.setText("€ " + totalReimbursement);
		totalReimbursementAmountLabel.setStyle("");
	}
	
	private String mapCategory (String category) {
		if (category == null || category.equals("alle")) return null;
		 return switch (category) {
	        case "Supermarkt" -> "SUPERMARKET";
	        case "Restaurant" -> "RESTAURANT";
	        default -> null;
	    };
	}
	
	private String mapStatus(String status) {
		if (status == null || status.equals("alle")) return null;
	    return switch (status) {
	        case "offen" -> "PENDING";
	        case "abgelehnt" -> "REJECTED";
	        case "genehmigt" -> "APPROVED";
	        case "zur Kontrolle" -> "FLAGGED";
	        default -> null;
	    };
	}
	
	private String translateStauts(String status) {
		if (status == null) return "alle";
		else {
			return switch (status.toUpperCase()) {
			case "APPROVED" -> "genehmigt";
			case "PENDING" -> "offen";
			case "REJECTED" -> "abgelehnt";
			case "FLAGGED" -> "zur Kontrolle";
			default -> "alle";
			};
		}
	}
	
	//for testing
	public void setReimbursementService(ReimbursementService service) {
		this.reimbursementService = service;
		
	}

	public void loadReimbursementsForUser(User selectedUser) {
		if (selectedUser != null) {
			reimbursementService = new ReimbursementService(selectedUser);
			List<Reimbursement> reimbursements = reimbursementService.getAllReimbursements();
			reimbursementHistoryTable.getItems().setAll(reimbursements);
		}
	}
}
