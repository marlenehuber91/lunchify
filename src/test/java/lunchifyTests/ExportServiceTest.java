package lunchifyTests;

import backend.logic.ExportService;
import backend.logic.StatisticsService;
import backend.model.InvoiceCategory;
import backend.model.Reimbursement;
import backend.model.User;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExportServiceTest {

    private ExportService exportService = new ExportService();
    private StatisticsService statisticsServiceMock;
    private User currentUser;
    
    @BeforeEach
    void setup() {
        statisticsServiceMock = mock(StatisticsService.class);
        currentUser = new User();
        currentUser.setName("TestUser");
        exportService = new ExportService(statisticsServiceMock, currentUser);
        exportService.setReportParameters("Anzahl pro Monat", "01.2023 - 04.2023");
    }
    
    @Test
    void exportToJson_ShouldNotThrowException() throws Exception {
        var data = Collections.<Reimbursement>emptyList();
        File tempFile = File.createTempFile("test", ".json");
        tempFile.deleteOnExit();

        assertDoesNotThrow(() -> exportService.exportToJson(data, tempFile));
    }

    @Test
    void exportToXml_ShouldNotThrowException() throws Exception {
        var data = Collections.<Reimbursement>emptyList();
        File tempFile = File.createTempFile("test", ".xml");
        tempFile.deleteOnExit();

        assertDoesNotThrow(() -> exportService.exportToXml(data, tempFile));
    }

    @Test
    void exportAdminToCsv_WithVariousReportTypes_ShouldNotThrow() throws Exception {
        File tempFile = File.createTempFile("test", ".csv");
        tempFile.deleteOnExit();

        // Mock die Statistik-Daten, damit Methoden aufgerufen werden können
        when(statisticsServiceMock.getInvoiceCountLastYear()).thenReturn(Map.of("Jan", 5));
        when(statisticsServiceMock.getReimbursementSumPerMonthLastYear()).thenReturn(Map.of("Jan", 123.45));
        when(statisticsServiceMock.getAverageInvoicesPerUserLastYear()).thenReturn(Map.of("User1", 2.5));
        when(statisticsServiceMock.getCountByCategory()).thenReturn(Map.of(InvoiceCategory.RESTAURANT, 10.0));
        when(statisticsServiceMock.getSumByCategory()).thenReturn(Map.of(InvoiceCategory.RESTAURANT, 150.0));

        List<String> reportTypes = List.of(
                "Anzahl pro Monat",
                "Erstattungsbetrag",
                "Rechnungen pro Nutzer",
                "Kategorien - Anzahl",
                "Kategorien - Summe",
                "Nicht unterstützt"
        );

        for (String reportType : reportTypes) {
            exportService.setReportParameters(reportType, null);
            assertDoesNotThrow(() -> exportService.exportAdminToCsv(tempFile, reportType, List.of(new Reimbursement())));
        }
    }
  
    /*
     * no further testing for pdf, csv, xml and json export, because the export involves Charts
     * which is JavaFX specific
     */
}
