package frontend.controller;

import java.io.IOException;
import java.util.List;
import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.logic.UserService;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import backend.model.UserRole;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ReimbursementHistoryController {

	User user;
	ReimbursementService reimbursementService;
	UserService userService;
	List<Reimbursement> reimbursements;
	String month;
	String year;
	String category;
	String status;
	String totalReimbursement;
	int currUserId;

	@FXML
	private TableView<Reimbursement> reimbursementHistoryTable;
	@FXML
	private TableColumn<Reimbursement, String> invoiceDate;
	@FXML
	private TableColumn<Reimbursement, String> processedDate;
	@FXML
	private TableColumn<Reimbursement, String> invoiceCategory;
	@FXML
	private TableColumn<Reimbursement, Float> invoiceAmount;
	@FXML
	private TableColumn<Reimbursement, Float> reimbursementAmount;
	@FXML
	private TableColumn<Reimbursement, String> reimbursementState;
	@FXML 
	private TableColumn<Reimbursement, String> userEmailColumn;
	@FXML
	private TableColumn<Reimbursement, Image> editColumn;
	@FXML
	private TableColumn<Reimbursement, Image> deleteColumn;
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
	private ComboBox<String> userFilterBox;
	@FXML
	private Text textTotalReimb;

	@FXML
	private Rectangle resetFilterButton;
	@FXML
	private Label userFilterLabel;

	@FXML
	void initialize() {
		if (user == null) {
			user = SessionManager.getCurrentUser();
		}
		
		reimbursementService = new ReimbursementService(user);
		reimbursements = reimbursementService.getAllReimbursements(user.getId());
		
		if (user.getRole()!= UserRole.ADMIN) {
			userFilterBox.setVisible(false);
			userFilterLabel.setVisible(false);

			editColumn.setVisible(false);
			deleteColumn.setVisible(false);
		}
		
		populateBoxes();
		loadList();
		
		//falls immer geupdatet werden soll
		monthFilterBox.setOnAction(e -> handleFilter(null));
	    yearFilterBox.setOnAction(e -> handleFilter(null));
	    categoryFilterBox.setOnAction(e -> handleFilter(null));
	    statusFilterBox.setOnAction(e -> handleFilter(null));
	    userFilterBox.setOnAction(e -> handleFilter(null));
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
		loadList();
		textTotalReimb.setText("Summe Rückerstattungen: '" + translateStatus(status)+ "'" );
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

		processedDate.setCellValueFactory(
				cellData -> new SimpleStringProperty(cellData.getValue().getProcessedDate().toString()));

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
		
		userEmailColumn.setCellValueFactory(
			    cellData -> {
			        if (cellData.getValue().getInvoice().getUser() != null) {
			            return new SimpleStringProperty(cellData.getValue().getInvoice().getUser().getEmail());
			        } else {
			            return new SimpleStringProperty("Unbekannt");
			        }
			    });

		editColumn.setCellFactory(column -> new TableCell<>() {
			private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/pen.png")));

			{
				imageView.setFitWidth(20);
				imageView.setFitHeight(20);
				imageView.setPreserveRatio(true);
				imageView.setCursor(Cursor.HAND);
			}

			@Override
			protected void updateItem(Image item, boolean empty) {
				super.updateItem(item, empty);

				if (empty) {
					setGraphic(null);
					setOnMouseClicked(null);
				} else {
					Reimbursement selectedReimbursement = getTableView().getItems().get(getIndex());
					if (selectedReimbursement != null && selectedReimbursement.isReimbursementEditable() && user.getRole() == UserRole.ADMIN) {
						setGraphic(imageView);
						setOnMouseClicked(event -> {
							try {
								FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/views/AdminEditReimbursement.fxml"));
								AnchorPane anchorPane = loader.load();
								AdminEditReimbursementController controller = loader.getController();
								controller.setReimbursement(selectedReimbursement);

								Stage stage = (Stage) reimbursementHistoryTable.getScene().getWindow();
								stage.setScene(new Scene(anchorPane));
								stage.show();

							} catch (IOException e) {
								e.printStackTrace();
								showAlert("Fehler", "Seite konnte nicht geladen werden.");
							}
						});
					} else {
						setGraphic(null);
						setOnMouseClicked(null);
					}
				}
			}
		});


		deleteColumn.setCellFactory(column -> new TableCell<>() {
			private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/delete.png")));

			{
				imageView.setFitWidth(20);
				imageView.setFitHeight(20);
				imageView.setPreserveRatio(true);
				imageView.setCursor(Cursor.HAND);
			}

			@Override
			protected void updateItem(Image item, boolean empty) {
				super.updateItem(item, empty);

				if (empty) {
					setGraphic(null);
					setOnMouseClicked(null);
				} else {
					Reimbursement selectedReimbursement = getTableView().getItems().get(getIndex());
					if (selectedReimbursement != null && selectedReimbursement.isReimbursementEditable()) {
						setGraphic(imageView);
						setOnMouseClicked(event -> {
							showDeleteConfirmationDialog(selectedReimbursement);
							loadList();
						});
					} else {
						setGraphic(null);
						setOnMouseClicked(null);
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
		List<String> emailUser = UserService.getAllUsersEmail();
		emailUser.add("alle");
		
		userFilterBox.setItems(FXCollections.observableArrayList(emailUser));
		userFilterBox.setValue(user.getEmail());
	}

	public void getFilterInput() {
		month = "alle".equals(monthFilterBox.getValue()) ? null : monthFilterBox.getValue();
		year = "alle".equals(yearFilterBox.getValue()) ? null : yearFilterBox.getValue();
		category = mapCategory(categoryFilterBox.getValue());
		status = mapStatus (statusFilterBox.getValue());
		
		String selectedUserEmail = userFilterBox.getValue();
		int targetUserId;

		if (selectedUserEmail == null || selectedUserEmail.equals("alle")) {
			reimbursements = reimbursementService.getFilteredReimbursements(month, year, category, status, -1);
		} else {
			targetUserId = UserService.getUserIdByEmail(selectedUserEmail);
			reimbursements = reimbursementService.getFilteredReimbursements(month, year, category, status, targetUserId);
		}
		
		loadList();
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
	
	private String translateStatus(String status) {
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
	
	public void setUserService (UserService service) {
		this.userService = service;
	}

	public void loadReimbursementsForUser(User selectedUser) {
		if (selectedUser != null) {
			reimbursementService = new ReimbursementService(selectedUser);
			List<Reimbursement> reimbursements = reimbursementService.getAllReimbursements(selectedUser.getId());
			reimbursementHistoryTable.getItems().setAll(reimbursements);
		}
	}

	private void showDeleteConfirmationDialog(Reimbursement toDeleteReimb) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Bestätigung");
		alert.setHeaderText(null);
		String date = String.valueOf(toDeleteReimb.getInvoice().getDate());
		String user = String.valueOf(toDeleteReimb.getInvoice().toString());
		alert.setContentText("Wollen Sie diesen Rückerstattungsantrag vom " +
						date + " löschen?");

		ButtonType buttonSave = new ButtonType("Ja");
		ButtonType buttonCancel = new ButtonType("Nein", ButtonBar.ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonSave, buttonCancel);
		alert.showAndWait().ifPresent(response -> {
			if (response == buttonSave) {
				boolean isReimbDeleted = reimbursementService.deleteReimbursement(toDeleteReimb);

				if (isReimbDeleted) {
					showAlert("Erfolg", "Rückerstattungsantrag gelöscht.");
				}
			}
		});
	}

	protected void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
