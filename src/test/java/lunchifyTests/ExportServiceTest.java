package lunchifyTests;

import backend.logic.ExportService;
import backend.model.Reimbursement;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class ExportServiceTest {

    private final ExportService exportService = new ExportService();

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
}
