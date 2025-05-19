package lunchifyTests;

import backend.logic.ExportService;
import backend.model.Reimbursement;
import backend.model.User;
import backend.model.UserRole;
import backend.model.UserState;
import javafx.scene.shape.Path;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertTrue;
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

    /*
     * no further testing for pdf, csv, xml and json export, because the export involves Charts
     * which is JavaFX specific
     */
}
