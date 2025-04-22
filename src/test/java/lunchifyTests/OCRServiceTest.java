package lunchifyTests;

import backend.logic.CategoryAnalyzer;
import backend.logic.OCRService;
import backend.model.Invoice;
import backend.model.InvoiceCategory;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.TesseractException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class OCRServiceTest {

    private OCRService ocrService;
    private ITesseract mockTesseract;

    @BeforeEach
    public void setUp() {
        mockTesseract = mock(ITesseract.class);
        ocrService = new OCRService();
        ocrService = spy(ocrService);
        doReturn(mockTesseract).when(ocrService).getTesseract();
    }

    @Test
    public void supermarketInvoice() throws Exception {
        ITesseract mockTesseract = mock(ITesseract.class);

        File imageFile = new File("src/test/resources/supermarketValidDate.jpg");

        String mockOCRText = "Bezahlt: 6,27 EUR\nDatum: 02.04.2025\nKategorie: Supermarkt";
        when(mockTesseract.doOCR(any(BufferedImage.class))).thenReturn(mockOCRText);

        Invoice invoice = ocrService.extractData(imageFile);

        assertNotNull(invoice, "Invoice shouldn´t be null");
        assertEquals(6.27f, invoice.getAmount(), 0.01, "Amount should be 6,27 Euro");
        assertEquals(LocalDate.of(2025, 4, 2), invoice.getDate(), "Date should be correct 2.4.25");
        assertEquals(InvoiceCategory.SUPERMARKET, invoice.getCategory(), "Category is supermarket");
    }

    @Test
    public void invalidInvoice() throws Exception {
        File invalidImageFile = new File("src/test/resources/invalidInvoice.jpg");

        Invoice invoice = ocrService.extractData(invalidImageFile);
        String extractedText = ocrService.extractText(invalidImageFile);
        assertEquals(InvoiceCategory.UNDETECTABLE, invoice.getCategory(), "Category is undetectable");
    }

    @Test
    public void restaurantInvoice() throws Exception {
        ITesseract mockTesseract = mock(ITesseract.class);

        File imageFile = new File("src/test/resources/Restaurant.jpg");

        String mockOCRText = "Bezahlt: 6,27 EUR\nDatum: 02.04.2025\nKategorie: Supermarkt";
        when(mockTesseract.doOCR(any(BufferedImage.class))).thenReturn(mockOCRText);

        Invoice invoice = ocrService.extractData(imageFile);

        assertNotNull(invoice, "Invoice shouldn´t be null");
        assertEquals(40.60f, invoice.getAmount(), 0.50, "Amount should be 40,60 Euro");
        assertEquals(LocalDate.of(2017, 10, 28), invoice.getDate(), "Date should be 28.10.2017");
        assertEquals(InvoiceCategory.RESTAURANT, invoice.getCategory(), "Category is restauant");
    }






}