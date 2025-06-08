package backend.logic;

import backend.interfaces.ConnectionProvider;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.ReimbursementState;
import backend.model.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import database.DatabaseConnection;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.Chart;
import javafx.scene.chart.PieChart;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.util.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

/**
 * The ExportService class provides functionality to export reimbursement data in the formats
 * JSON, XML, PDF, and CSV. It supports different types of reports for both administrators
 * and regular users, with options to customize the output based on report type and time range.
 *
 * <p>The service integrates with StatisticsService to generate statistical reports and charts,
 * and can export data for individual users or aggregated data for accounting purposes.</p>
 *
 * <p>Supported export formats include:
 * <ul>
 *   <li>JSON - For reimbursement data and accounting reports</li>
 *   <li>XML - For reimbursement data and accounting reports</li>
 *   <li>PDF - For visual reports with charts and tables</li>
 *   <li>CSV - For tabular data exports (admin only)</li>
 * </ul>
 * </p>
 */

public class ExportService {// AI generated changed by the team

	private StatisticsService statisticsService;
	private User currentUser;
	private String reportType;
	private String timeRange;

	/**
	 * Constructs a new ExportService with default settings.
	 */
	public ExportService() {

	}

	/**
	 * Constructs a new ExportService with the specified statistics service and current user.
	 *
	 * @param statisticsService the statistics service to use for report generation
	 * @param currentUser the user initiating the export operation
	 */
	public ExportService(StatisticsService statisticsService, User currentUser) {
		this.statisticsService = statisticsService;
		this.currentUser = currentUser;
	}

	/** secures DatabaseConnection */
    public static ConnectionProvider connectionProvider = new ConnectionProvider() {
        @Override
        public Connection getConnection() {
            return DatabaseConnection.connect();
        }
    };

	/**
	 * Sets the report parameters for the export operation.
	 *
	 * @param reportType the type of report to generate (e.g., "Anzahl pro Monat", "Erstattungsbetrag")
	 * @param timeRange the time range for the report (e.g., "2024, "May 2025")
	 */
	public void setReportParameters(String reportType, String timeRange) {
		this.reportType = reportType;
		this.timeRange = timeRange;
	}

	/**
	 * Exports a list of reimbursements to a JSON file.
	 *
	 * @param data the list of reimbursements to export
	 * @param file the target file to write the JSON data to
	 * @throws Exception if an error occurs during JSON serialization or file writing
	 */
	public void exportToJson(List<Reimbursement> data, File file) throws Exception { // AI generated
        ObjectMapper mapper = new ObjectMapper();
        // Java 8 Date/Time-Unterstützung aktivieren
        mapper.registerModule(new JavaTimeModule());
        // Deaktiviert das Schreiben von Dates als Timestamps (z. B. 1623456000000)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.writeValue(file, data);

    }

	/**
	 * Exports a list of reimbursements to an XML file.
	 *
	 * @param data the list of reimbursements to export
	 * @param file the target file to write the XML data to
	 * @throws Exception if an error occurs during XML marshalling or file writing
	 */
    public void exportToXml(List<Reimbursement> data, File file) throws Exception {
		// Kontext muss alle beteiligten Klassen kennen
		JAXBContext context = JAXBContext.newInstance(Wrapper.class, Reimbursement.class, Invoice.class, User.class);

		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(new Wrapper(data), file);
	}

	/**
	 * Exports accounting data (payroll information) to a JSON file.
	 *
	 * @param userPayrollData a map of users to their total approved amounts
	 * @param file the target file to write the JSON data to
	 * @throws Exception if an error occurs during JSON serialization or file writing
	 */
	public void exportToJsonAccounting(Map<User, Double> userPayrollData, File file) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		mapper.registerModule(new JavaTimeModule());
		mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

		List<Map<String, Object>> accountingData = new ArrayList<>();

		userPayrollData.forEach((user, amount) -> {
			Map<String, Object> entry = new LinkedHashMap<>();
			entry.put("userId", user.getId());
			entry.put("userName", user.getName());
			entry.put("totalApprovedAmount", amount);
			entry.put("currency", "EUR");
			accountingData.add(entry);
		});

		mapper.writeValue(file, accountingData);
	}

	@XmlRootElement
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class AccountingWrapper {
		@XmlElement(name = "accountingEntry")
		List<AccountingEntry> entries = new ArrayList<>();
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class AccountingEntry {
		int userId;
		String userName;
		String userEmail;
		double totalApprovedAmount;
		String currency = "EUR";

		// Default-Konstruktor für JAXB
		public AccountingEntry() {}

		public AccountingEntry(User user, double amount) {
			this.userId = user.getId();
			this.userName = user.getName(); // ggf. user.getFirstName() + " " + user.getLastName()
			this.userEmail = user.getEmail();
			this.totalApprovedAmount = amount;
		}
	}

	/**
	 * Exports accounting data (payroll information) to an XML file.
	 *
	 * @param userPayrollData a map of users to their total approved amounts
	 * @param file the target file to write the XML data to
	 * @throws Exception if an error occurs during XML marshalling or file writing
	 */
	public void exportToXmlAccounting(Map<User, Double> userPayrollData, File file) throws Exception {
		AccountingWrapper wrapper = new AccountingWrapper();
		userPayrollData.forEach((user, amount) -> {
			wrapper.entries.add(new AccountingEntry(user, amount));
		});

		JAXBContext context = JAXBContext.newInstance(AccountingWrapper.class);
		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(wrapper, file);
	}

	/**
	 * Generates an admin PDF report with charts and data tables.
	 *
	 * @param file the target PDF file
	 * @param chart the chart to include in the report
	 * @param reportTitle the title of the report
	 * @param adminData the reimbursement data to include in the report
	 * @throws IOException if an error occurs during PDF generation
	 */
	public void exportAdminToPdf(File file, Chart chart, String reportTitle, List<Reimbursement> adminData)
			throws IOException {
		statisticsService.setReimbursements(adminData);
		try (PDDocument doc = new PDDocument()) {
			PDPage page = new PDPage(PDRectangle.A4);
			doc.addPage(page);

			try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
				// 1. Header
				addPdfHeader(content, reportTitle);

				// 2. Chart
				if (chart != null) {
					addChart(content, doc, chart, 50, 720, 0.6f);
				}

				// 3. Daten-Tabelle
				addAdminDataTable(content, doc, reportTitle, 400);
			}
			doc.save(file);
		}
	}

	/**
	 * Generates a user PDF report with pie and bar charts.
	 *
	 * @param file the target PDF file
	 * @param pieChart the pie chart to include
	 * @param barChart the bar chart to include
	 * @param filteredData the filtered reimbursement data for the current user
	 * @throws IOException if an error occurs during PDF generation
	 */
	public void exportUserToPdf(File file, PieChart pieChart, BarChart<?, ?> barChart, List<Reimbursement> filteredData)
			throws IOException {
		statisticsService.setReimbursements(filteredData); // WICHTIG

		try (PDDocument doc = new PDDocument()) {
			PDPage page = new PDPage(PDRectangle.A4);
			doc.addPage(page);

			try (PDPageContentStream content = new PDPageContentStream(doc, page)) {
				// Header
				addPdfHeader(content, "Meine Statistiken");

				// Charts
				addChart(content, doc, pieChart, 50, 725, 0.35f);
				addChart(content, doc, barChart, 320, 725, 0.35f);

				content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
				content.beginText();
				content.newLineAtOffset(120, 580);
				content.showText("Kategorien");
				content.newLineAtOffset(200, 0);
				content.showText("Status");
				content.endText();

				// Tabellen
				addUserDataTables(content, doc, 500);
			}

			doc.save(file);
		}
	}

	/**
	 * Exports admin data to a CSV file based on the specified report type.
	 *
	 * @param file the target CSV file
	 * @param reportType the type of report to generate
	 * @param adminData the reimbursement data to export
	 * @throws IOException if an error occurs during CSV file writing
	 */
	public void exportAdminToCsv(File file, String reportType, List<Reimbursement> adminData) throws IOException {
		statisticsService.setReimbursements(adminData);
		try (PrintWriter writer = new PrintWriter(file)) {
			switch (reportType) {
			case "Anzahl pro Monat":
				writer.println("Monat,Anzahl");
				statisticsService.getInvoiceCountLastYear().forEach((k, v) -> writer.println(k + "," + v));
				break;
			case "Erstattungsbetrag":
				writer.println("Monat,Summe (€)");
				statisticsService.getReimbursementSumPerMonthLastYear()
						.forEach((k, v) -> writer.println(k + "," + String.format("%.2f", v)));
				break;
			case "Rechnungen pro Nutzer":
				writer.println("Nutzer,Ø Rechnungen");
				statisticsService.getAverageInvoicesPerUserLastYear()
					.forEach((k, v) -> writer.println(k + "," + String.format("%.2f", v)));
				break;

			case "Kategorien - Anzahl":
				writer.println("Kategorie,Anzahl");
				statisticsService.getCountByCategory()
					.forEach((k, v) -> writer.println(k.name() + "," + v.intValue()));
				break;

			case "Kategorien - Summe":
				writer.println("Kategorie,Summe (€)");
				statisticsService.getSumByCategory()
					.forEach((k, v) -> writer.println(k.name() + "," + String.format("%.2f", v)));
				break;

			default:
				writer.println("Report-Typ nicht unterstützt: " + reportType);
			}
		}
	}
	/**
	 * Adds a formatted header section to a PDF document.
	 *
	 * @param content The PDPageContentStream to write to
	 * @param title The title text to display in the header
	 * @throws IOException If an I/O error occurs while writing to the PDF
	 */
	private void addPdfHeader(PDPageContentStream content, String title) throws IOException {
		content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
		content.beginText();
		content.newLineAtOffset(50, 800); // Höhere Startposition
		content.showText(title);
		content.endText();

		content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
		content.beginText();
		content.newLineAtOffset(50, 780);
		content.showText("Erstellt am: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
		content.newLineAtOffset(0, -15); // Zeilenabstand
		content.showText("Von: " + currentUser.getName());
		content.endText();

		content.beginText();
		content.newLineAtOffset(50, 750);
		content.showText("Zeitraum: " + (timeRange != null ? timeRange : "Alle Zeiträume"));
		content.endText();

		content.setLineWidth(1f);
		content.moveTo(50, 735);
		content.lineTo(550, 735);
		content.stroke();
	}

	/**
	 * Adds a chart image to the PDF document at specified position and scale.
	 *
	 * @param content The PDPageContentStream to write to
	 * @param doc The PDDocument instance
	 * @param chart The chart to be rendered (can be null)
	 * @param x The x-coordinate position
	 * @param y The y-coordinate position
	 * @param scale The scaling factor (0.0-1.0)
	 * @throws IOException If an I/O error occurs while writing to the PDF
	 */
	private void addChart(PDPageContentStream content, PDDocument doc, Chart chart, float x, float y, float scale)
			throws IOException {
		if (chart == null)
			return;

		BufferedImage image = SwingFXUtils.fromFXImage(chart.snapshot(new SnapshotParameters(), null), null);
		PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
		content.drawImage(pdImage, x, y - (pdImage.getHeight() * scale), pdImage.getWidth() * scale,
				pdImage.getHeight() * scale);
	}

	/**
	 * Adds an admin data table to the PDF document based on the report type.
	 *
	 * @param content The PDPageContentStream to write to
	 * @param doc The PDDocument instance
	 * @param reportType The type of report determining the data to display
	 * @param startY The vertical starting position for the table
	 * @throws IOException If an I/O error occurs while writing to the PDF
	 */
	private void addAdminDataTable(PDPageContentStream content, PDDocument doc, String reportType, float startY)
			throws IOException {

		float currentY = startY;
		// Tabellenkopf
		content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
		content.beginText();
		content.newLineAtOffset(50, currentY);
		content.showText("Datenübersicht (" + reportType + ")");
		content.endText();
		currentY -= 20;

		Map<String, String> data = new LinkedHashMap<>();
		switch (reportType) {
		case "Anzahl pro Monat":
			statisticsService.getInvoiceCountLastYear().forEach((k, v) -> data.put(k, String.valueOf(v)));
			break;

		case "Erstattungsbetrag":
			statisticsService.getReimbursementSumPerMonthLastYear()
					.forEach((k, v) -> data.put(k, String.format("%.2f €", v)));
			break;

		case "Rechnungen pro Nutzer":
			statisticsService.getAverageInvoicesPerUserLastYear()
					.forEach((k, v) -> data.put(k, String.format("%.2f", v)));
			break;

		case "Kategorien - Anzahl":
			statisticsService.getCountByCategory().forEach((k, v) -> data.put(k.name(), String.valueOf(v.intValue())));
			break;

		case "Kategorien - Summe":
			statisticsService.getSumByCategory().forEach((k, v) -> data.put(k.name(), String.format("%.2f €", v)));
			break;
		}

		addTableContent(content, doc, data, currentY);
	}

	/**
	 * Adds user-specific data tables to the PDF document including category sums and status distribution.
	 *
	 * @param content The PDPageContentStream to write to
	 * @param doc The PDDocument instance
	 * @param startY The vertical starting position for the tables
	 * @throws IOException If an I/O error occurs while writing to the PDF
	 */
	private void addUserDataTables(PDPageContentStream content, PDDocument doc, float startY) throws IOException {

		float currentY = startY;
		PDPageContentStream currContent = content;

		currContent.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
		currContent.beginText();
		currContent.newLineAtOffset(50, currentY);
		currContent.showText("Kategorien (Summe)");
		currContent.endText();

		currContent.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
		currContent.beginText();
		currContent.newLineAtOffset(50, currentY - 25);
		currContent.showText("Kategorie");
		currContent.newLineAtOffset(150, 0);
		currContent.showText("Betrag");
		currContent.endText();

		currentY -= 40;
		currContent.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);

		Map<InvoiceCategory, Double> categoryData = statisticsService.getSumByCategory();

		for (Map.Entry<InvoiceCategory, Double> entry : categoryData.entrySet()) {
			if (currentY < 100) {
				currContent.close();
				PDPage newPage = new PDPage(PDRectangle.A4);
				doc.addPage(newPage);
				currContent = new PDPageContentStream(doc, newPage);
				currentY = 750;
			}

			currContent.beginText();
			currContent.newLineAtOffset(50, currentY);
			currContent.showText(entry.getKey().name()); // Enum zu String konvertieren
			currContent.newLineAtOffset(150, 0);
			currContent.showText(String.format("%.2f €", entry.getValue()));
			currContent.endText();
			currentY -= 20;
		}

		float statusStartY = currentY - 30;
		currContent.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
		currContent.beginText();
		currContent.newLineAtOffset(50, statusStartY);
		currContent.showText("Statusverteilung");
		currContent.endText();

		currContent.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
		currContent.beginText();
		currContent.newLineAtOffset(50, statusStartY - 25);
		currContent.showText("Status");
		currContent.newLineAtOffset(150, 0);
		currContent.showText("Anzahl");
		currContent.endText();

		currContent.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
		currentY = statusStartY - 40;
		Map<String, Integer> statusData = getStatusData();

		for (Map.Entry<String, Integer> entry : statusData.entrySet()) {
			if (currentY < 100) {
				currContent.close();
				PDPage newPage = new PDPage(PDRectangle.A4);
				doc.addPage(newPage);
				currContent = new PDPageContentStream(doc, newPage);
				currentY = 750;
			}

			currContent.beginText();
			currContent.newLineAtOffset(50, currentY);
			currContent.showText(entry.getKey());
			currContent.newLineAtOffset(150, 0);
			currContent.showText(String.valueOf(entry.getValue()));
			currContent.endText();
			currentY -= 20;
		}
	}

	/**
	 * Generates status distribution data for reimbursements.
	 *
	 * @return A map containing status descriptions and their counts
	 */
	private Map<String, Integer> getStatusData() {
		Map<String, Integer> counts = new LinkedHashMap<>();
		statisticsService.getReimbursements().forEach(r -> {
			String status = switch (r.getStatus()) {
			case APPROVED -> "Genehmigt";
			case REJECTED -> "Abgelehnt";
			case PENDING -> "Offen";
			case FLAGGED -> "Zur Kontrolle";
			};
			counts.merge(status, 1, Integer::sum);
		});

		return counts;
	}

	/**
	 * Adds tabular content to the PDF document with automatic pagination.
	 *
	 * @param content The PDPageContentStream to write to
	 * @param doc The PDDocument instance
	 * @param data The data to display in the table
	 * @param startY The vertical starting position for the table
	 * @throws IOException If an I/O error occurs while writing to the PDF
	 */
	private void addTableContent(PDPageContentStream content, PDDocument doc, Map<String, String> data, float startY)
			throws IOException {
		float currentY = startY;
		PDPageContentStream currContent = content;

		// Tabellenkopf
		currContent.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
		currContent.beginText();
		currContent.newLineAtOffset(50, currentY);
		currContent.showText("Beschreibung");
		currContent.newLineAtOffset(200, 0);
		currContent.showText("Wert");
		currContent.endText();
		currentY -= 15;

		// Tabelleninhalt
		currContent.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
		for (Map.Entry<String, String> entry : data.entrySet()) {
			if (currentY < 50) { // Seitenumbruch
				currContent.close();
				PDPage newPage = new PDPage(PDRectangle.A4);
				doc.addPage(newPage);
				currContent = new PDPageContentStream(doc, newPage);
				currentY = 750;
			}

			currContent.beginText();
			currContent.newLineAtOffset(50, currentY);
			currContent.showText(entry.getKey());
			currContent.newLineAtOffset(200, 0);
			currContent.showText(entry.getValue());
			currContent.endText();
			currentY -= 15;
		}
	}

	// Wrapper-Klasse für die Liste
	@XmlRootElement(name = "reimbursements")
	private static class Wrapper {
		@XmlElement(name = "reimbursement")
		private List<Reimbursement> items;

		public Wrapper() {
		}

		public Wrapper(List<Reimbursement> items) {
			this.items = items;
		}
    }
	/**
	 * Data transfer object for exporting user reimbursement information.
	 */
	public static class ExportData {
		private int userId;
		private String userName;
		private List<Reimbursement> reimbursements;
		private long totalAmount;

		public ExportData(int userId, String userName) {
			this.userId = userId;
			this.userName = userName;
			this.reimbursements = new ArrayList<>();
			this.totalAmount = 0L;
		}

		public int getUserId() {
			return userId;
		}

		public String getUserName() {
			return userName;
		}

		public List<Reimbursement> getReimbursements() {
			return reimbursements;
		}

		public void setReimbursements(List<Reimbursement> reimbursements) {
			this.reimbursements = reimbursements;
			calculateTotalAmount();
		}

		public long getTotalAmount() {
			return totalAmount;
		}

		private void calculateTotalAmount() {
			this.totalAmount = reimbursements.stream()
					.filter(r -> r.getStatus() == ReimbursementState.APPROVED)
					.mapToLong(r -> (long) r.getApprovedAmount())
					.sum();
		}
	}
}
