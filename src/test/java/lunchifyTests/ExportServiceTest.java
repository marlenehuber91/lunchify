package lunchifyTests;

import backend.logic.ExportService;
import backend.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExportServiceTest { //strong AI help used for tests
    @Mock
    private File mockFile;

    @Mock
    private User mockUser;
    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private JAXBContext jaxbContext;

    @Mock
    private Marshaller marshaller;

    @InjectMocks
    private ExportService exportService;

    private List<Reimbursement> createTestData() {
        // Mock Invoice mit allen benötigten Parametern
        Invoice invoice = new Invoice(
                LocalDate.now(),
                300.0f,
                InvoiceCategory.RESTAURANT,
                mockFile,
                mockUser
        );

        return Arrays.asList(
                new Reimbursement(invoice, 250.0f, LocalDate.now(), ReimbursementState.APPROVED)
        );
    }

    // --- JSON Export Tests ---
    @Test
    void exportToJson_Success() throws Exception {
        File tempFile = File.createTempFile("test", ".json");
        List<Reimbursement> data = createTestData();

        exportService.exportToJson(data, tempFile);

        assertTrue(tempFile.exists());
        assertTrue(tempFile.length() > 0);
        tempFile.delete();
    }

    @Test
    void exportToJson_HandlesIOException() throws Exception {
        doThrow(new IOException("Test Error")).when(objectMapper).writeValue(any(File.class), any());

        assertThrows(IOException.class, () ->
                exportService.exportToJson(createTestData(), new File("invalid/path.json")));
    }

    // --- XML Export Tests ---
    @Test
    void exportToXml_Success() throws Exception {
        File tempFile = File.createTempFile("test", ".xml");
        List<Reimbursement> data = createTestData();

        when(jaxbContext.createMarshaller()).thenReturn(marshaller);

        exportService.exportToXml(data, tempFile);

        verify(marshaller).setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        assertTrue(tempFile.exists());
        tempFile.delete();
    }

    @Test
    void exportToXml_JAXBException() throws Exception {
        when(jaxbContext.createMarshaller()).thenThrow(new JAXBException("Test Error"));

        assertThrows(JAXBException.class, () ->
                exportService.exportToXml(createTestData(), new File("test.xml")));
    }
}
