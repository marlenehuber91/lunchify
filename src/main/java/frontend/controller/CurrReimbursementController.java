package frontend.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.logic.StatisticsService;
import backend.model.InvoiceCategory;
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
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class CurrReimbursementController {

	User user;
	ReimbursementService reimbursementService;
	StatisticsService statisticsService;

	@FXML
	private TableView<Reimbursement> currReimbursementTable;
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
	private Label totalReimbursementAmountLabel, chartLabel, sumLabel, totalSumLabel, noDataLabel;

	@FXML
	private Text currentMonthText;

	@FXML
	private PieChart userPieChart, sumChart;

	public void setReimbursementService(ReimbursementService reimbursementService) {
		this.reimbursementService = reimbursementService;
	}

	@FXML
	void initialize() {
		if (user == null) {
			user = SessionManager.getCurrentUser();
		}

		getCurrMonth();
		if (reimbursementService == null) {
			reimbursementService = new ReimbursementService(user);
		}

		statisticsService = new StatisticsService();
		statisticsService.setReimbursements(reimbursementService.getAllReimbursements(user.getId()));

		loadList();
		loadUserPieChart();
		loadCategoryPieChart();

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
	public void handleUserChoice() {
		Reimbursement selectedReimbursement = currReimbursementTable.getSelectionModel().getSelectedItem();
		User selectedUser = selectedReimbursement.getInvoice().getUser();

		if (selectedReimbursement != null && selectedReimbursement.isReimbursementUserEditable(user.getId())) {

			if (user != null) {
				try {
					FXMLLoader loader = new FXMLLoader(
							getClass().getResource("/frontend/views/EditReimbursement.fxml"));
					AnchorPane editReimbursementPane = loader.load();

					EditReimbursementController controller = loader.getController();
					controller.setSelectedUser(selectedUser);
					System.out.println(
							"controller.setSelectedUser(selectedUser)" + controller.getSelectedUser().getEmail());
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

	@FXML
	public void openStatistics(MouseEvent event) {
		try {
			FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/frontend/views/Statistics.fxml"));
			Parent root = fxmlLoader.load();

			Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

			stage.setTitle("Search");
			stage.setScene(new Scene(root));
			stage.show();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadList() {
		List<Reimbursement> reimbursements = reimbursementService.getCurrentReimbursements(user.getId());
		String totalReimbursement = String.valueOf(reimbursementService.getTotalReimbursement(reimbursements));
		totalReimbursementAmountLabel.setText("€ " + totalReimbursement);
		totalSumLabel.setText("Rückerstattung " + getCurrMonth() + ": € " + totalReimbursement);
		totalReimbursementAmountLabel.setStyle("");
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
						setText(item);
						setText("genehmigt");
						setStyle("-fx-background-color: green; -fx-text-fill: white;");
					}
				}
			}
		});

		currReimbursementTable.setItems(reimbursementList);
	}

	private String getCurrMonth() {
		LocalDate currDate = LocalDate.now();
		Locale germanLocale = new Locale("de", "DE");
		String currMonth = currDate.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
		currentMonthText.setText("Rechnungen " + currMonth);
		return currMonth;
	}

	public Label getTotalReimbursementAmountLabel() {
		return this.totalReimbursementAmountLabel;
	}

	public Object loadCurrentReimbursements() {
		// TODO Auto-generated method stub
		return null;
	}

	private void loadUserPieChart() {
		    // Hole nur Erstattungen der letzten 12 Monate
		    List<Reimbursement> last12Months = statisticsService.getReimbursementsFromLast12Months();
		    
		    // Erstelle Verteilung der Kategorien
		    Map<String, Integer> distribution = statisticsService.getCategoryDistribution(last12Months);
		    
		    if (last12Months == null || last12Months.isEmpty() || distribution == null || distribution.isEmpty()) {
		    	noDataLabel.setText("In den letzten 12 \n" + "wurde keine Rückerstattung \n" + "eingereicht");
		    	userPieChart.setData(FXCollections.observableArrayList());
		    	chartLabel.setVisible(false);
		    	userPieChart.setVisible(false);
		    	totalSumLabel.setVisible(false);
		    	return;
		    }
		    	 // Konvertiere zur ObservableList für das PieChart
		    ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

		    int total = distribution.values().stream().mapToInt(Integer::intValue).sum();
		    String labelContent = "";

		    for (java.util.Map.Entry<String, Integer> entry : distribution.entrySet()) {
		        String category = entry.getKey();
		        int value = entry.getValue();
		        double percent = (double) value / total * 100;

		        PieChart.Data data = new PieChart.Data(
		            String.format("%s: %.1f%% (%d)", category, percent, value),
		            value
		        );
		        if (entry.getKey().equals("SUPERMARKET")) {
		        	labelContent += "SUPERMARKT: " + entry.getValue() + "\n";
		        } else {
		        	labelContent += entry.getKey() + ": " + entry.getValue() + "\n";
		        }
		        pieChartData.add(data);
		    }
		    chartLabel.setText(labelContent);

		    userPieChart.setData(pieChartData);

		    userPieChart.setTitle("Rechnungen pro Kategorie (letzte 12 Monate)");
		    userPieChart.lookup(".chart-title").setStyle("-fx-font-size: 10px;"); // kleinere Überschrift
		    
		}

	private void loadCategoryPieChart() {
		Map<InvoiceCategory, Double> categorySumMap = statisticsService.getSumByCategory();

		ObservableList<PieChart.Data> pieChartData = FXCollections.observableArrayList();

		if (categorySumMap == null || categorySumMap.isEmpty() || pieChartData == null || pieChartData.isEmpty()) {
			noDataLabel.setText("In den letzten 12 \n" + "wurde keine Rückerstattung \n" + "eingereicht");
			noDataLabel.setVisible(true);
	        sumLabel.setVisible(false);
	        sumChart.setVisible(false);
	        sumChart.setData(FXCollections.observableArrayList());
	        totalSumLabel.setVisible(false);
	        return;
		} 
	
		String labelContent = "";

		for (Map.Entry<InvoiceCategory, Double> entry : categorySumMap.entrySet()) {
			if (entry.getKey().equals("SUPERMARKET")) {
				labelContent += "SUPERMARKT: € " + String.format("%.2f", entry.getValue()) + "\n";
			} else {
				labelContent += entry.getKey().name() + ": € " + String.format("%.2f", entry.getValue()) + "\n";
			}
			pieChartData.add(new PieChart.Data(entry.getKey().name(), entry.getValue()));
		}

		sumLabel.setText(labelContent);

		sumChart.setData(pieChartData);

		sumChart.setTitle("Verteilung der genehmigten Rückerstattungsbeträge je Kategorie (letzte 12 Monate)");
		sumChart.lookup(".chart-title").setStyle("-fx-font-size: 10px;");
	}

}
