package frontend.controller;

import java.io.File;
import java.time.LocalDate;
import java.util.List;

import backend.logic.InvoiceService;
import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.User;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class BaseUploadController {
	
	private static BaseUploadController instance; //for testing
	
	@FXML
	protected StackPane uploadPane;
	@FXML
	protected ImageView uploadedImageView;
	@FXML
	protected Text uploadText;
	@FXML
	protected Text previewText;
	@FXML
	protected Button submitButton;
	@FXML
	protected ComboBox<InvoiceCategory> categoryBox;
	@FXML
	protected TextField amountField;
	@FXML
	protected TextField reimbursementAmountField;
	@FXML
	protected DatePicker datePicker;
	@FXML
	protected Label amountLabel;
	@FXML
	protected Label datePickerLabel;
	@FXML
	protected Label imageUploadLabel;
	@FXML
	protected TextArea infoText;
	@FXML
	protected File uploadedFile;
	protected User user;
	protected InvoiceService invoiceService = new InvoiceService();
	protected ReimbursementService reimbursementService;
	protected List<Invoice> invoices;
	protected User selectedUser;
	
	public BaseUploadController() { //for testing
		instance = this;
	}

	@FXML
	public void initialize() {
		// Initialisierungslogik hier, die für beide Controller verwendet wird
		if (user == null) {
			user = SessionManager.getCurrentUser();
		}

		invoiceService = new InvoiceService(user);
		invoices = invoiceService.getInvoices();
		reimbursementService = new ReimbursementService(user);

		submitButton.setDisable(true);

		categoryBox.setItems(FXCollections.observableArrayList(InvoiceCategory.values()));
		infoText.setText(reimbursementService.getInfoText());

		amountField.textProperty().addListener((obs, oldVal, newVal) -> {
			boolean isAmountValid = invoiceService.isValidFloat(newVal);
			updateLabel(amountLabel, isAmountValid, "Kein gültiger Zahlenwert", "Betrag eingegeben");

			checkFields();
		});

		categoryBox.valueProperty().addListener((obs, oldVal, newVal) -> {
			checkFields();
		});

		datePicker.setDayCellFactory(picker -> new javafx.scene.control.DateCell() {
			@Override
			public void updateItem(LocalDate date, boolean empty) {
				super.updateItem(date, empty);

				LocalDate today = LocalDate.now();
				LocalDate firstDayOfMonth = today.withDayOfMonth(1);

				boolean isDisabled = date.isBefore(firstDayOfMonth) || date.isAfter(today);

				setDisable(isDisabled);
				if (isDisabled) {
					setStyle("-fx-background-color: #d3d3d3;");
				}
			}
		});

		datePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (newVal == null) {
				updateLabel(datePickerLabel, false, "Kein Datum ausgewählt!", "");
			} else if (newVal.isBefore(LocalDate.now().withDayOfMonth(1))) {
				updateLabel(datePickerLabel, false, "Datum darf nicht vor dem aktuellen Monat liegen", "");
			} else if (newVal.isAfter(LocalDate.now())) {
				updateLabel(datePickerLabel, false, "Datum darf nicht in der Zukunft liegen", "");
			} else if (!invoiceService.isWorkday(newVal)) {
				updateLabel(datePickerLabel, false, "Kein gültiger Arbeitstag!", "");
			} else {
				updateLabel(datePickerLabel, true, "", "Datum eingegeben");
			}

			checkFields();
		});

		uploadedImageView.setOnMouseClicked(event -> {
			handleImageClick(event);
		});
	}

	@FXML
	protected void openFileChooser() {
		Stage stage = (Stage) uploadPane.getScene().getWindow();
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Bild oder PDF hochladen");

		fileChooser.getExtensionFilters()
				.addAll(new FileChooser.ExtensionFilter("Bilddateien und PDF", "*.jpg", "*.jpeg", "*.png", "*.pdf"));

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
				uploadedImageView.setImage(null);
				showAlert("Datei hochgeladen", "Die Datei wurde erfolgreich ausgewählt:\n" + filePath);
			}

			uploadText.setText("Foto hochgeladen");

		} else {
			showAlert("Keine Datei", "Es wurde keine Datei ausgewählt.");
		}
	}

	@FXML
	private void handleImageClick(MouseEvent event) { // created by AI		
		 if (event.getClickCount() == 2 && uploadedImageView.getImage() != null) {
		        // Neues Fenster für die Bildansicht
		        Stage stage = new Stage();
		        stage.setTitle("Bildansicht");

		        ImageView largeImageView = new ImageView(uploadedImageView.getImage());
		        largeImageView.setPreserveRatio(true); // Bildverhältnis beibehalten
		        largeImageView.setFitWidth(600); // Anfangsbreite

		        // ScrollPane mit dem ImageView
		        ScrollPane scrollPane = new ScrollPane(largeImageView);
		        scrollPane.setFitToHeight(true);
		        scrollPane.setFitToWidth(true);

		        // Zoom-Funktionalität hinzufügen
		        scrollPane.setOnScroll(e -> {
		            double delta = e.getDeltaY();
		            double scaleFactor = delta > 0 ? 1.1 : 0.9; // Zoom-In oder Zoom-Out

		            // Zoom um den Mauszeigerpunkt herum berechnen
		            double scrollH = scrollPane.getHvalue();
		            double scrollV = scrollPane.getVvalue();

		            largeImageView.setScaleX(largeImageView.getScaleX() * scaleFactor);
		            largeImageView.setScaleY(largeImageView.getScaleY() * scaleFactor);

		            // Scroll-Position wiederherstellen
		            scrollPane.setHvalue(scrollH);
		            scrollPane.setVvalue(scrollV);
		        });

		        Scene scene = new Scene(scrollPane, 800, 600); // ScrollPane als root
		        stage.setScene(scene);
		        stage.show();
		    }
	}

	protected void setReimbursementAmount(String amountText) {
		float invoiceAmount = Float.parseFloat(amountText);
		float limit = reimbursementService.getLimit(categoryBox.getValue());
		float reimbursementAmount = categoryBox.getValue().calculateReimbursement(invoiceAmount, limit);
		reimbursementAmountField.setText(String.valueOf(reimbursementAmount));
		reimbursementService.setReimbursementAmount(reimbursementAmount);
	}

	protected void showAlert(String title, String content) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle(title);
		alert.setHeaderText(null);
		alert.setContentText(content);
		alert.showAndWait();
	}

	protected void checkFields() {
		String amountText = amountField.getText().trim();
		LocalDate date = datePicker.getValue();
		boolean isValidDate = isDateValid(date);
		boolean isAmountValid = invoiceService.isAmountValid(amountText);
		boolean isCategorySelected = categoryBox.getValue() != null;
		boolean isFileUploaded = uploadedFile != null;

		if (isCategorySelected && isAmountValid) {
			setReimbursementAmount(amountText);
		}

		boolean allFieldsFilled = isAmountValid && isValidDate && isCategorySelected && isFileUploaded;
		submitButton.setDisable(!allFieldsFilled);
	}

	protected void updateLabel(Label label, boolean isValid, String errorText, String successText) {
		if (!isValid) {
			label.setText(errorText);
			label.setStyle("-fx-text-fill: red;");
		} else {
			label.setText(successText);
			label.setStyle("-fx-text-fill: green");
		}
	}

	private boolean isDateValid(LocalDate date) {
		return (date != null && !date.isBefore(LocalDate.now().withDayOfMonth(1)) && !date.isAfter(LocalDate.now())
				&& invoiceService.isWorkday(date));
	}
	
	public void setSelectedFile(File dummyFile) {
		this.uploadedFile = dummyFile;
		checkFields();
	}
	
	public static BaseUploadController getInstance() {
        return instance;
    }
	
	public File getSelectedFile () {
		return uploadedFile;
	}
}