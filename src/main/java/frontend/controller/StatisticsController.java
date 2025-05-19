package frontend.controller;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import backend.logic.ExportService;
import backend.logic.ReimbursementService;
import backend.logic.SessionManager;
import backend.logic.StatisticsService;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import backend.model.UserRole;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.Chart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;



public class StatisticsController {

	User user;
	List<Reimbursement> reimbursements;
	List<Reimbursement> allReimbursements;
	StatisticsService statisticsService;
	StatisticsService adminStatisticsService;
	ExportService exportService;
	ReimbursementService reimbursementService;
	String selectedMonth, selectedType, selectedYear, report, timeRange;
	boolean isAdminTab;
	Tab selectedTab;
	List<Reimbursement> currentFilteredUserData;
	List<Reimbursement> currentFilteredAdminData;
	
	@FXML
	private Circle backArrow;

	@FXML
	private StackPane backButton;

	@FXML
	private PieChart pieChart;

	@FXML
	private PieChart adminPieChart;

	@FXML
	private BarChart<String, Number> statusBarChart;

	@FXML
	private BarChart<String, Number> adminBarChart;

	@FXML
	private AnchorPane statistscPane;

	@FXML
	private ComboBox<String> monthFilterComboBox;

	@FXML
	private ComboBox<String> yearFilterComboBox;

	@FXML
	private ComboBox<String> typeFilterComboBox;

	@FXML
	private ComboBox<String> reportTypeComboBox;
	
	@FXML
	private ComboBox<String> reportTimeRangeComboBox;
	
	@FXML
	private Tab adminTab;
	
	@FXML
	private TabPane statisticsTabPane;

	@FXML
	private Label noDataLabel;
	
	@FXML
	private Label detailLabel;

	@FXML
	private CheckBox rejectedCheckBox;
	
	@FXML
	private Button exportPDFButton;
	
	@FXML
	private Button exportCSVButton;
	
	@FXML
	private Rectangle containerRectangle;
	
	@FXML
	void initialize() {
		if (user == null) {
			user = SessionManager.getCurrentUser();
		}
		
		if (user.getRole() != UserRole.ADMIN) {
			adminTab.setDisable(true);
			adminTab.setText("");
		}
		
		setServices();
		populateBoxes();
		setIntialValues();
		setEventhandlers();
		handleFilter(null);
		handleAdminFilter(null);
		
	}

	@FXML
	void handleBackToDashboard(MouseEvent event) {
		String role = (user.getRole() == UserRole.ADMIN) ? "AdminDashboard" : "UserDashboard";

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

		List<Reimbursement> filtered = reimbursementService.getFilteredReimbursements(
		        selectedMonth, 
		        selectedYear, 
		        null, //all categories
		        null, //all states
		        user.getId()
		);
		
		
		List<Reimbursement> pieData = rejectedCheckBox.isSelected() ? filtered
				: filtered.stream().filter(r -> r.getStatus() != ReimbursementState.REJECTED).toList();
		currentFilteredUserData = filtered;
		
		if (pieData.isEmpty()) {
			noDataLabel.setVisible(true);
			pieChart.getData().clear();
			pieChart.setTitle("");
			return;
		} 
		
		noDataLabel.setVisible(false);
		
		Platform.runLater(() -> {
		    // Update des Diagramms nach der Berechnung
		    loadStatusBarChart(filtered);
		    loadPieChart(filtered, pieChart);
		});
		
		selectedTab = statisticsTabPane.getSelectionModel().getSelectedItem();
		isAdminTab = selectedTab == adminTab;
		adjustLayoutForExportButton();
	}
	

    @FXML
    private void handleExportPDF() {
        File file = showFileChooser("PDF speichern", "*.pdf");
        if (file != null) {
            try {
                if (isAdminTab) {
                    Chart activeChart = adminBarChart.isVisible() ? adminBarChart : adminPieChart;
                    exportService.exportAdminToPdf(file, activeChart, reportTypeComboBox.getValue(), currentFilteredAdminData);
                } else {
                    exportService.exportUserToPdf(file, pieChart, statusBarChart, currentFilteredUserData);
                }
                showAlert("Export erfolgreich", "PDF wurde erstellt");
            } catch (IOException e) {
                showAlert("Export fehlgeschlagen", "Fehler: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleExportCSV() {        
        File file = showFileChooser("CSV speichern", "*.csv");
        if (file != null) {
            try {
                exportService.exportAdminToCsv(file, reportTypeComboBox.getValue(), currentFilteredAdminData);
                showAlert("Export erfolgreich", "CSV wurde erstellt");
            } catch (IOException e) {
                showAlert("Export fehlgeschlagen", "Fehler: " + e.getMessage());
            }
        }
    }
	
	private void handleAdminFilter(MouseEvent event) {
		//resetCharts();
		getFilterInput();
		
		adminBarChart.getData().clear();
		adminPieChart.getData().clear();
		adminBarChart.setVisible(false);
		adminPieChart.setVisible(false);
		detailLabel.setVisible(false);
		reportTimeRangeComboBox.setVisible(false);
		
		switch (report) {
	        case "Anzahl pro Monat" -> {
	            adminBarChart.setVisible(true);
	            loadAvgMonthChart();
	        }
	        case "Rechnungen pro Nutzer" -> {
	            adminBarChart.setVisible(true);
	            loadAvgEmp();
	        }
	        case "Erstattungsbetrag" -> {
	            adminBarChart.setVisible(true);
	            loadMonthlyReimbursementSumChart();
	        }
	        case "Kategorien - Summe" -> {
	            adminPieChart.setVisible(true);
	            if ((timeRange).contains("12")) {
	            	List<Reimbursement> filtered = adminStatisticsService.getReimbursementsFromLast12Months();
	            	adminStatisticsService.setReimbursements(filtered);
	            } 
	            
	            loadChartWithData(adminStatisticsService.getSumByCategory(), adminPieChart, false);
	            reportTimeRangeComboBox.setVisible(true);
	        }
	        case "Kategorien - Anzahl" -> {
	        	
	        	if ((timeRange).contains("12")) {
	            	List<Reimbursement> filtered = adminStatisticsService.getReimbursementsFromLast12Months();
	            	adminStatisticsService.setReimbursements(filtered);
	            }
	        	
	            adminPieChart.setVisible(true);
	            loadChartWithData(adminStatisticsService.getCountByCategory(), adminPieChart, true);
	            reportTimeRangeComboBox.setVisible(true);
	        }			
		}
		currentFilteredAdminData = getFilteredAdminDataForCurrentTimeRange();
		
	}

	// created by AI changed by the team
	private void loadPieChart(List<Reimbursement> reimbursements, PieChart chart) {
		chart.getData().clear();
		statisticsService.setReimbursements(reimbursements);
		String type = typeFilterComboBox.getValue();

		if ("Anzahl".equals(type)) {
			Map<InvoiceCategory, Double> countByCategory = statisticsService.getCountByCategory();
			for (var entry : countByCategory.entrySet()) {
				String label = formatCategoryName(entry.getKey()) + " (" + entry.getValue() + ")";
				chart.getData().add(new PieChart.Data(label, entry.getValue()));
			}
			chart.setTitle("Anzahl der Rückerstattungsbeträge");
		} else {
			Map<InvoiceCategory, Double> sumByCategory = statisticsService.getSumByCategory();
			for (var entry : sumByCategory.entrySet()) {
				String label = formatCategoryName(entry.getKey()) + " (" + String.format("%.2f €", entry.getValue())
						+ ")";
				chart.getData().add(new PieChart.Data(label, entry.getValue()));
			}
			chart.setTitle("Summe Rückerstattungsbeträge");
		}
	}

	// created by AI
	public void loadChartWithData(Map<InvoiceCategory, Double> categorySums, PieChart chart, boolean isCount) {
		chart.getData().clear();
		chart.setTitle(null);
		chart.setLabelsVisible(true);
		chart.setLegendVisible(true);
		chart.applyCss();
		chart.layout();

		for (Map.Entry<InvoiceCategory, Double> entry : categorySums.entrySet()) {
			String categoryName = formatCategoryName(entry.getKey());
			double amount = entry.getValue();
			
			String label = isCount
					? categoryName + " (" + String.format("%.0f", amount) + ")"
					: categoryName + " (" + String.format("%.2f €", amount) + ")";

			PieChart.Data slice = new PieChart.Data(label, amount);
			chart.getData().add(slice);
		}
		String title = isCount ? "Anzahl der Rückerstattungen" : "Summe der Rückerstattungen";
		chart.setTitle(title);
	}
	
	
	// created by AI
	private void loadStatusBarChart(List<Reimbursement> reimbursements) {
		Map<String, Integer> statusCount = new LinkedHashMap<>(Map.of("Offen", 0, "Genehmigt", 0, "Abgelehnt", 0, "Zur Kontrolle", 0));

		for (Reimbursement r : reimbursements) {
			String status = switch (r.getStatus()) {
			case PENDING -> "Offen";
			case APPROVED -> "Genehmigt";
			case REJECTED -> "Abgelehnt";
			case FLAGGED -> "Zur Kontrolle";
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

	private void loadAvgMonthChart() {
		Map<String, Integer> counts = adminStatisticsService.getInvoiceCountLastYear();
		
		XYChart.Series<String, Number> series = new XYChart.Series<>();
		series.setName("Rechnungen pro Monat");
		
		int sum = 0;

		for (Map.Entry<String, Integer> entry : counts.entrySet()) {
			series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
			sum += entry.getValue();
		}
	
		detailLabel.setText("Gesamt eingereicht Rechnungen für\ndie letzten 12 Monate : " + sum);
		detailLabel.setVisible(true);
		adminBarChart.getData().add(series);
	}

	private void loadAvgEmp() {
		Map<String, Double> counts = adminStatisticsService.getAverageInvoicesPerUserLastYear();
		XYChart.Series<String, Number> series = new XYChart.Series<>();
		
		NumberAxis yAxis = (NumberAxis) adminBarChart.getYAxis();
		yAxis.setAutoRanging(true);
		
		series.setName("Durchschnitt pro Nutzer");

		for (Entry<String, Double> entry : counts.entrySet()) {
			series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
		}
		
		double total = counts.values().stream().mapToDouble(Double::doubleValue).sum();
		double average = counts.size() > 0 ? total/counts.size() : 0;
		
		detailLabel.setText(String.format("Durschnittliche Anzahl Rechnungen \npro Nutzer (12 Monate):  %.2f",  average));
		detailLabel.setVisible(true);
		
		adminBarChart.getData().add(series);
	}

	private void loadMonthlyReimbursementSumChart() {
		Map<String, Double> sums = adminStatisticsService.getReimbursementSumPerMonthLastYear();
		XYChart.Series<String, Number> series = new XYChart.Series<>();
		series.setName("Erstattungen pro Monat (€)");
		
		NumberAxis yAxis = (NumberAxis) adminBarChart.getYAxis();
		yAxis.setAutoRanging(true);
		
		double totalSum = 0;

		for (Map.Entry<String, Double> entry : sums.entrySet()) {
			XYChart.Data<String, Number> data = new XYChart.Data<>(entry.getKey(), entry.getValue());
			series.getData().add(data);
			totalSum += entry.getValue();
		}
		
		detailLabel.setText(String.format("Gesamtsumme (12 Monate): %.2f € ", totalSum));
		detailLabel.setVisible(true);
		
		adminBarChart.getData().add(series);
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
		reportTypeComboBox.setItems(FXCollections.observableArrayList("Anzahl pro Monat", "Erstattungsbetrag", "Rechnungen pro Nutzer",
				"Kategorien - Anzahl", "Kategorien - Summe"));
		reportTimeRangeComboBox.setItems(FXCollections.observableArrayList("Alle Zeiträume", "letzten 12 Monate"));
	}

	private void getFilterInput() {
		 selectedType = typeFilterComboBox.getValue() != null ? typeFilterComboBox.getValue() : "Summe";
	     selectedMonth = "alle".equals(monthFilterComboBox.getValue()) || monthFilterComboBox.getValue() == null ? null : monthFilterComboBox.getValue();
	     selectedYear = "alle".equals(yearFilterComboBox.getValue()) || yearFilterComboBox.getValue() == null ? null : yearFilterComboBox.getValue();
	     report = reportTypeComboBox.getValue();
	     timeRange = reportTimeRangeComboBox.getValue();
	}

	private void setEventhandlers() {
		monthFilterComboBox.setOnAction(e -> handleFilter(null));
		yearFilterComboBox.setOnAction(e -> handleFilter(null));
		typeFilterComboBox.setOnAction(e -> handleFilter(null));
		rejectedCheckBox.setOnAction(e -> handleFilter(null));
		reportTypeComboBox.setOnAction(e -> handleAdminFilter(null));
		reportTimeRangeComboBox.setOnAction(e -> handleAdminFilter(null));
		statisticsTabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
		    isAdminTab = newTab == adminTab;
		    adjustLayoutForExportButton();
		});
	}
	
	private void setServices () {
		reimbursementService = new ReimbursementService(user);
		statisticsService = new StatisticsService();
		reimbursements = reimbursementService.getAllReimbursements(user.getId());
		statisticsService.setReimbursements(reimbursements);
		
		adminStatisticsService = new StatisticsService();
		allReimbursements = reimbursementService.getAllReimbursements(null);
		adminStatisticsService.setReimbursements(allReimbursements);
		
		 exportService = new ExportService(statisticsService, SessionManager.getCurrentUser());
	}

	// created by AI, enhanced by the team
	private void setIntialValues() {
		LocalDate currDate = LocalDate.now();

		String monthName = currDate.getMonth().getDisplayName(TextStyle.FULL, Locale.GERMAN);
		monthName = Character.toUpperCase(monthName.charAt(0)) + monthName.substring(1);

		selectedMonth = monthName;
		selectedYear = String.valueOf(currDate.getYear());

		monthFilterComboBox.setValue(selectedMonth);
		yearFilterComboBox.setValue(selectedYear);
		reportTypeComboBox.setValue("Anzahl pro Monat");
		reportTimeRangeComboBox.setValue("alle Zeiträume");
	}
	
	// Hilfsmethoden
    private File showFileChooser(String title, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(title, extension));
        return fileChooser.showSaveDialog(statistscPane.getScene().getWindow());
    }
    

    private void showAlert(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }
    
    private void adjustLayoutForExportButton() {
        exportCSVButton.setVisible(isAdminTab);
        exportCSVButton.setDisable(!isAdminTab);
        
        if (isAdminTab) containerRectangle.setHeight(173); 
        else containerRectangle.setHeight(77);
    }
    
    private List<Reimbursement> getFilteredAdminDataForCurrentTimeRange() {
        return timeRange != null && timeRange.contains("12")
            ? adminStatisticsService.getReimbursementsFromLast12Months()
            : adminStatisticsService.getReimbursements();
    }
	
}
