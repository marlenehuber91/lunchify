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
import frontend.controller.ReimbursementHistoryController;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Marshaller;
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
import java.util.ArrayList;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

public class ExportService {// AI generated changed by the team

	private StatisticsService statisticsService;
	private User currentUser;
	private String reportType;
	private String timeRange;

	public ExportService() {

	}

	public ExportService(StatisticsService statisticsService, User currentUser) {
		this.statisticsService = statisticsService;
		this.currentUser = currentUser;
	}

    public static ConnectionProvider connectionProvider = new ConnectionProvider() {
        @Override
        public Connection getConnection() {
            return DatabaseConnection.connect();
        }
    };

	public void setReportParameters(String reportType, String timeRange) {
		this.reportType = reportType;
		this.timeRange = timeRange;
	}

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

	public void exportToXml(List<Reimbursement> data, File file) throws Exception {
		// Kontext muss alle beteiligten Klassen kennen
		JAXBContext context = JAXBContext.newInstance(Wrapper.class, Reimbursement.class, Invoice.class, User.class);

		Marshaller marshaller = context.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(new Wrapper(data), file);
	}

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

				content.setFont(PDType1Font.HELVETICA_BOLD, 10);
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

	// CSV Export (nur für Admin)
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

	private void addPdfHeader(PDPageContentStream content, String title) throws IOException {
		content.setFont(PDType1Font.HELVETICA_BOLD, 16);
		content.beginText();
		content.newLineAtOffset(50, 800); // Höhere Startposition
		content.showText(title);
		content.endText();

		content.setFont(PDType1Font.HELVETICA, 10);
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

	private void addChart(PDPageContentStream content, PDDocument doc, Chart chart, float x, float y, float scale)
			throws IOException {
		if (chart == null)
			return;

		BufferedImage image = SwingFXUtils.fromFXImage(chart.snapshot(new SnapshotParameters(), null), null);
		PDImageXObject pdImage = LosslessFactory.createFromImage(doc, image);
		content.drawImage(pdImage, x, y - (pdImage.getHeight() * scale), pdImage.getWidth() * scale,
				pdImage.getHeight() * scale);
	}

	private void addAdminDataTable(PDPageContentStream content, PDDocument doc, String reportType, float startY)
			throws IOException {

		float currentY = startY;
		// Tabellenkopf
		content.setFont(PDType1Font.HELVETICA_BOLD, 12);
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

	private void addUserDataTables(PDPageContentStream content, PDDocument doc, float startY) throws IOException {

		float currentY = startY;
		PDPageContentStream currContent = content;

		currContent.setFont(PDType1Font.HELVETICA_BOLD, 12);
		currContent.beginText();
		currContent.newLineAtOffset(50, currentY);
		currContent.showText("Kategorien (Summe)");
		currContent.endText();

		currContent.setFont(PDType1Font.HELVETICA_BOLD, 10);
		currContent.beginText();
		currContent.newLineAtOffset(50, currentY - 25);
		currContent.showText("Kategorie");
		currContent.newLineAtOffset(150, 0);
		currContent.showText("Betrag");
		currContent.endText();

		currentY -= 40;
		currContent.setFont(PDType1Font.HELVETICA, 10);

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
		currContent.setFont(PDType1Font.HELVETICA_BOLD, 12);
		currContent.beginText();
		currContent.newLineAtOffset(50, statusStartY);
		currContent.showText("Statusverteilung");
		currContent.endText();

		currContent.setFont(PDType1Font.HELVETICA_BOLD, 10);
		currContent.beginText();
		currContent.newLineAtOffset(50, statusStartY - 25);
		currContent.showText("Status");
		currContent.newLineAtOffset(150, 0);
		currContent.showText("Anzahl");
		currContent.endText();

		currContent.setFont(PDType1Font.HELVETICA, 10);
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

	private void addTableContent(PDPageContentStream content, PDDocument doc, Map<String, String> data, float startY)
			throws IOException {
		float currentY = startY;
		PDPageContentStream currContent = content;

		// Tabellenkopf
		currContent.setFont(PDType1Font.HELVETICA_BOLD, 10);
		currContent.beginText();
		currContent.newLineAtOffset(50, currentY);
		currContent.showText("Beschreibung");
		currContent.newLineAtOffset(200, 0);
		currContent.showText("Wert");
		currContent.endText();
		currentY -= 15;

		// Tabelleninhalt
		currContent.setFont(PDType1Font.HELVETICA, 10);
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

    private class ExportData {
            private int userId;
            private String userName;
            private List<Reimbursement> reimbursements;
            private long totalAmount;

            public ExportData(int userId, String userName, List<Reimbursement> reimbursements, long totalAmount) {
                this.userId = userId;
                this.userName = userName;
                this.reimbursements = reimbursements;
                this.totalAmount = totalAmount;
            }

            public int getUserId() {
                return userId;
            }
            public String getUserName() {
                return userName;
            }

        public long getTotalAmount(List<Reimbursement> reimbursements) {
            return reimbursements.stream()
                    .filter(r -> r.getStatus() == ReimbursementState.APPROVED)
                    .mapToLong(r -> (long) r.getApprovedAmount())
                    .sum();
        }

        public ExportData(User user, List<Reimbursement> reimbursements) {
                userId = user.getId();
                userName = user.getName();




        }



    }
}
