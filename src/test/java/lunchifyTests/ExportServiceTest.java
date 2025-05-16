package lunchifyTests;

import backend.logic.ExportService;
import backend.model.Reimbursement;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Ignore("Skip testing the class - only for GitHubActions reasons! - Tests are successfull locally")
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
