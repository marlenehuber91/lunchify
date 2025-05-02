package frontend.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.logic.StatisticsService;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import backend.model.UserRole;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

public class StatisticsController {

	User user;
	List<Reimbursement> reimbursements;
	StatisticsService statisticService;
	ReimbursementService reimbursementService;
	String selectedMonth;
	String selectedYear;
	String selectedType;

	@FXML
	private Circle backArrow;

	@FXML
	private StackPane backButton;

	@FXML
	private PieChart pieChart;
	
	@FXML
	private BarChart<String, Number> statusBarChart;

	@FXML
	private AnchorPane statistscPane;

	@FXML
	private ComboBox<String> monthFilterComboBox;

	@FXML
	private ComboBox<String> yearFilterComboBox;
	
	@FXML
    private ComboBox<String> typeFilterComboBox;
	
	@FXML
	private Label noDataLabel;
	
	@FXML 
	private CheckBox rejectedCheckBox;

	@FXML
	void initialize() {
		if (user == null) {
			user = SessionManager.getCurrentUser();
		}
		reimbursementService = new ReimbursementService(user);
		statisticService= new StatisticsService();

		populateBoxes();
		setIntialValues();
		handleFilter(null);
		
		monthFilterComboBox.setOnAction(e -> handleFilter(null));
	    yearFilterComboBox.setOnAction(e -> handleFilter(null));
	    typeFilterComboBox.setOnAction(e -> handleFilter(null));
	    rejectedCheckBox.setOnAction(e -> handleFilter(null));
	}

	@FXML
	void handleBackToDashboard(MouseEvent event) {
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
	private void handleFilter(MouseEvent e) {
		getFilterInput();

		List<Reimbursement> filtered = reimbursementService.getFilteredReimbursements(selectedMonth, selectedYear,
				null, null, user.getId());
		
		
		List<Reimbursement> pieData = rejectedCheckBox.isSelected()
				? filtered
				: filtered.stream()
						  .filter(r -> r.getStatus() != ReimbursementState.REJECTED)
						  .toList();
		
		if (pieData.isEmpty()) {
			noDataLabel.setVisible(true);
			pieChart.getData().clear();
			pieChart.setTitle("");
			return;
		} else {
			noDataLabel.setVisible(false);
			loadPieChart(pieData);
		}
		
		loadStatusBarChart(filtered);
	}
	//created by AI
	private void loadPieChart(List<Reimbursement> reimbursements) {
		pieChart.getData().clear();
		String type = typeFilterComboBox.getValue();

		if ("Anzahl".equals(type)) {
			Map<InvoiceCategory, Integer> countByCategory = getCountByCategory(reimbursements);
			for (var entry : countByCategory.entrySet()) {
				String label = formatCategoryName(entry.getKey()) + " (" + entry.getValue() + ")";
				pieChart.getData().add(new PieChart.Data(label, entry.getValue()));
			}
			pieChart.setTitle("Anzahl der Rückerstattungsbeträge");
		} else {
			Map<InvoiceCategory, Double> sumByCategory = getSumByCategory(reimbursements);
			for (var entry : sumByCategory.entrySet()) {
				String label = formatCategoryName(entry.getKey()) + " (" + String.format("%.2f €", entry.getValue()) + ")";
				pieChart.getData().add(new PieChart.Data(label, entry.getValue()));
			}
			pieChart.setTitle("Summe Rückerstattungsbeträge");
		}
	}
	
	//created by AI
	public void loadChartWithData(Map<InvoiceCategory, Double> categorySums) {
		pieChart.getData().clear();

		for (Map.Entry<InvoiceCategory, Double> entry : categorySums.entrySet()) {
			String categoryName = formatCategoryName(entry.getKey());
			double amount = entry.getValue();

			PieChart.Data slice = new PieChart.Data(categoryName + " (" + String.format("%.2f €", amount) + ")",
					amount);
			pieChart.getData().add(slice);
		}
		pieChart.setTitle("Summe der Rückerstattungen");
	}
	
	//created by AI
	private void loadStatusBarChart(List<Reimbursement> reimbursements) {
		Map<String, Integer> statusCount = new LinkedHashMap<>(Map.of(
			"Offen", 0, "Genehmigt", 0, "Abgelehnt", 0
		));

		for (Reimbursement r : reimbursements) {
			String status = switch (r.getStatus()) {
				case PENDING -> "Offen";
				case APPROVED -> "Genehmigt";
				case REJECTED -> "Abgelehnt";
				default -> throw new IllegalArgumentException("Unexpected value: " + r.getStatus());
			};
			statusCount.merge(status, 1, Integer::sum);
		}

		statusBarChart.getData().clear();
	
		int maxValue = statusCount.values().stream().max(Integer::compareTo).orElse(0);
		int upperBound = Math.max(maxValue + 1, 10);

		for (var entry : statusCount.entrySet()) {
			XYChart.Series<String, Number> series = new XYChart.Series<>();
			series.setName(entry.getKey());
			XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
			series.getData().add(data);
			statusBarChart.getData().add(series);

			data.nodeProperty().addListener((obs, oldNode, newNode) -> {
				if (newNode != null) {
					Tooltip.install(newNode, new Tooltip(entry.getValue() + " " + entry.getKey()));
				}
			});
		}

		NumberAxis yAxis = (NumberAxis) statusBarChart.getYAxis();
		yAxis.setLowerBound(0); // Setze den minimalen Wert
	    yAxis.setUpperBound(upperBound);
	    yAxis.setTickUnit(1); // Setzt den Schrittwert auf 1
	    yAxis.setMinorTickVisible(false);
	    yAxis.setAutoRanging(false);
		yAxis.setTickLabelFormatter(new NumberAxis.DefaultFormatter(yAxis) {
			@Override
			public String toString(Number object) {
				return String.format("%d", object.intValue());
			}
		});

		statusBarChart.getXAxis().setTickLabelsVisible(false);
		statusBarChart.getXAxis().setTickMarkVisible(false);
		statusBarChart.getXAxis().setOpacity(0);
	}


	private String formatCategoryName(InvoiceCategory category) {
		String name = category.name().toLowerCase();
		return Character.toUpperCase(name.charAt(0)) + name.substring(1);
	}

	public void populateBoxes() {
		typeFilterComboBox.setItems(FXCollections.observableArrayList("Summe", "Anzahl"));
		typeFilterComboBox.setValue("Summe");
		monthFilterComboBox.setItems(FXCollections.observableArrayList("alle", "Jänner", "Februar", "März", "April",
				"Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"));
		yearFilterComboBox.setItems(FXCollections.observableArrayList("2024", "2025", "alle"));
	}

	public Map<InvoiceCategory, Double> getSumByCategory(List<Reimbursement> reimbursements) {
		statisticService.setReimbursements(reimbursements);
		return statisticService.getSumByCategory();
	}
	
	public Map<InvoiceCategory, Integer> getCountByCategory(List<Reimbursement> reimbursements) {
		Map<InvoiceCategory, Integer> result = new HashMap<>();
		for (Reimbursement r : reimbursements) {
			result.merge(r.getInvoice().getCategory(), 1, Integer::sum);
		}
		return result;
	}

	private void getFilterInput() {
		selectedType = typeFilterComboBox.getValue();
		selectedMonth = "alle".equals(monthFilterComboBox.getValue()) ? null : monthFilterComboBox.getValue();
		selectedYear = "alle".equals(yearFilterComboBox.getValue()) ? null : yearFilterComboBox.getValue();
	}

	//created by AI
	private void setIntialValues() {
		LocalDate currDate = LocalDate.now();
		
		String monthName = currDate.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN);
		monthName = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1);
		
		selectedMonth = monthName;
		selectedYear = String.valueOf(currDate.getYear());
		
		monthFilterComboBox.setValue(selectedMonth);
		yearFilterComboBox.setValue(selectedYear);
	}
	
}
