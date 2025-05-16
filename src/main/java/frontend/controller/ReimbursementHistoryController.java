package frontend.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import backend.logic.ExportService;
import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.logic.UserService;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import backend.model.UserRole;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class ReimbursementHistoryController {

	User user;
	User selectedUser;
	ReimbursementService reimbursementService;
	UserService userService;
	List<Reimbursement> reimbursements;
	String month;
	String year;
	String category;
	String status;
	String totalReimbursement;
	ExportService exportService;
	int currUserId;
	private boolean selfmade;

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
	private TableColumn<Reimbursement, Image> approveColumn;
	@FXML
	private TableColumn<Reimbursement, Image> rejectColumn;
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
	
	
	/**
	 * Initializes the reimbursement view.
	 * - Retrieves the current user from the session if not already set.
	 * - Instantiates the ReimbursementService with the current user.
	 * - Loads all reimbursements for the current user.
	 * - Hides UI elements (filter box, edit/delete columns) if the user is not an admin.
	 * - Populates dropdown boxes (e.g., filters).
	 * - Loads the reimbursement list into the view.
	 * - Sets event handlers for filter boxes to trigger list filtering when changed.
	 */
	@FXML
	void initialize() {
		if (user == null) {
			user = SessionManager.getCurrentUser();
		}

		if (selectedUser == null) {
			selectedUser = SessionManager.getCurrentUser();
		}

		reimbursementService = new ReimbursementService(user);
		reimbursements = reimbursementService.getAllReimbursements(user.getId());

		if (user.getRole()!= UserRole.ADMIN) {
			userFilterBox.setVisible(false);
			userFilterLabel.setVisible(false);

			editColumn.setVisible(false);
			deleteColumn.setVisible(false);
			approveColumn.setVisible(false);
			rejectColumn.setVisible(false);
		}

		populateBoxes();
		loadList();

		monthFilterBox.setOnAction(e -> handleFilter(null));
	    yearFilterBox.setOnAction(e -> handleFilter(null));
	    categoryFilterBox.setOnAction(e -> handleFilter(null));
	    statusFilterBox.setOnAction(e -> handleFilter(null));
	    userFilterBox.setOnAction(e -> handleFilter(null));
	    
	    selfmade = user.getId() == selectedUser.getId();
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
		selfmade = user == selectedUser;
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
					selectedUser = selectedReimbursement.getInvoice().getUser();
					if (selectedReimbursement != null && selectedReimbursement.isReimbursementEditable() && user.getRole() == UserRole.ADMIN) {
						setGraphic(imageView);
						setOnMouseClicked(event -> {
							try {
								FXMLLoader loader = new FXMLLoader(getClass().getResource("/frontend/views/AdminEditReimbursement.fxml"));
								AnchorPane anchorPane = loader.load();
								AdminEditReimbursementController controller = loader.getController();
								controller.setReimbursement(selectedReimbursement);
								controller.setSelectedUser(selectedUser);

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
					selectedUser = selectedReimbursement.getInvoice().getUser();
					if (selectedReimbursement != null && selectedReimbursement.isReimbursementEditable()) {
						setGraphic(imageView);
						setOnMouseClicked(event -> {
							showDeleteConfirmationDialog(selectedReimbursement, selectedReimbursement.getInvoice().getUser());
							reimbursements = reimbursementService.getAllReimbursements(user.getId());
							loadList();
						});
					} else {
						setGraphic(null);
						setOnMouseClicked(null);
					}
				}
			}
		});

		approveColumn.setCellFactory(column -> new TableCell<>() {
			private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/accept.png")));

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
					selectedUser = selectedReimbursement.getInvoice().getUser();
					if (selectedReimbursement != null && selectedReimbursement.isReimbursementAcceptable()) {
						setGraphic(imageView);
						setOnMouseClicked(event -> {
							showApproveConfirmationDialog(selectedReimbursement);
							reimbursements = reimbursementService.getAllReimbursements(user.getId());
							loadList();
						});
					} else {
						setGraphic(null);
						setOnMouseClicked(null);
					}
				}
			}
		});

		rejectColumn.setCellFactory(column -> new TableCell<>() {
			private final ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream("/frontend/images/cross.png")));

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
					selectedUser = selectedReimbursement.getInvoice().getUser();
					if (selectedReimbursement != null && selectedReimbursement.isReimbursementRejectable()) {
						setGraphic(imageView);
						setOnMouseClicked(event -> {
							showRejectConfirmationDialog(selectedReimbursement);
							reimbursements = reimbursementService.getAllReimbursements(user.getId());
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
		if (selectedUser!= null) {
			userFilterBox.setValue(selectedUser.getEmail());
		} else {
			userFilterBox.setValue(user.getEmail());
		}
	}

	public void getFilterInput() {
		month = "alle".equals(monthFilterBox.getValue()) ? null : monthFilterBox.getValue();
		year = "alle".equals(yearFilterBox.getValue()) ? null : yearFilterBox.getValue();
		category = mapCategory(categoryFilterBox.getValue());
		status = mapStatus (statusFilterBox.getValue());

		String selectedUserEmail = userFilterBox.getValue();
		selectedUser = userService.getUserByEmail(selectedUserEmail);
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

	private void showDeleteConfirmationDialog(Reimbursement toDeleteReimb, User reimbUser) {
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
				boolean isReimbDeleted = reimbursementService.deleteReimbursement(toDeleteReimb, reimbUser, selfmade);

				if (isReimbDeleted) {
					showAlert("Erfolg", "Rückerstattungsantrag gelöscht.");
				}
			}
		});
	}

	private void showApproveConfirmationDialog(Reimbursement toApproveReimb) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Bestätigung");
		alert.setHeaderText(null);
		String date = String.valueOf(toApproveReimb.getInvoice().getDate());
		String user = String.valueOf(toApproveReimb.getInvoice().toString());
		alert.setContentText("Wollen Sie diesen Rückerstattungsantrag vom " +
				date + " genehmigen?");

		ButtonType buttonSave = new ButtonType("Ja");
		ButtonType buttonCancel = new ButtonType("Nein", ButtonBar.ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonSave, buttonCancel);
		alert.showAndWait().ifPresent(response -> {
			if (response == buttonSave) {
				boolean isReimbAccepted = reimbursementService.approveReimbursement(toApproveReimb, toApproveReimb.getInvoice().getUser(), selfmade);

				if (isReimbAccepted) {
					showAlert("Erfolg", "Rückerstattungsantrag genehmigt.");
				}
			}
		});
	}

	private void showRejectConfirmationDialog(Reimbursement toRejectReimb) {
		Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
		alert.setTitle("Bestätigung");
		alert.setHeaderText(null);
		String date = String.valueOf(toRejectReimb.getInvoice().getDate());
		String user = String.valueOf(toRejectReimb.getInvoice().toString());
		alert.setContentText("Wollen Sie diesen Rückerstattungsantrag vom " +
				date + " ablehnen?");

		ButtonType buttonSave = new ButtonType("Ja");
		ButtonType buttonCancel = new ButtonType("Nein", ButtonBar.ButtonData.CANCEL_CLOSE);

		alert.getButtonTypes().setAll(buttonSave, buttonCancel);
		alert.showAndWait().ifPresent(response -> {
			if (response == buttonSave) {
				boolean isReimbRejeced = reimbursementService.rejectReimbursement(toRejectReimb, toRejectReimb.getInvoice().getUser(), selfmade);

				if (isReimbRejeced) {
					showAlert("Erfolg", "Rückerstattungsantrag abgelehnt.");
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

	protected void initializeForSelectedUser(User user) {
		this.selectedUser = user;
		this.user = SessionManager.getCurrentUser();
		this.reimbursementService = new ReimbursementService(user);
		this.reimbursements = reimbursementService.getAllReimbursements(user.getId());

		populateBoxes();
		loadList();
	}

	@FXML
	private void handleExport() { //AI generated
		List<Reimbursement> data = reimbursementHistoryTable.getItems();
		exportService = new ExportService();

		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Exportieren als");
		fileChooser.getExtensionFilters().addAll(
				new FileChooser.ExtensionFilter("JSON", "*.json"),
				new FileChooser.ExtensionFilter("XML", "*.xml")
		);

		File file = fileChooser.showSaveDialog(reimbursementHistoryTable.getScene().getWindow());
		if (file != null) {
			try {
				String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
				if ("json".equalsIgnoreCase(extension)) {
					exportService.exportToJson(data, file);
				} else if ("xml".equalsIgnoreCase(extension)) {
					exportService.exportToXml(data, file);
				}
				showAlert("Erfolg", "Daten wurden exportiert: " + file.getAbsolutePath());
			} catch (Exception e) {
				showAlert("Fehler", "Export fehlgeschlagen: " + e.getMessage());
			}
		}
	}
}

