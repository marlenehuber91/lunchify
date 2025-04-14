package backend.logic;

import java.io.File;

import backend.model.Invoice;
import backend.model.InvoiceCategory;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OCRService {

    private final ITesseract tesseract;
    private final CategoryAnalyzer categoryAnalyzer = new CategoryAnalyzer();


    public OCRService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("C:\\teaching-2025.ss.prse.braeuer.team4\\src\\main\\resources\\tessdata");
        tesseract.setLanguage("deu+eng");
    }

    public String extractText(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                System.err.println("Bild konnte nicht gelesen werden: " + imageFile.getPath());
                return null;
            }
            return tesseract.doOCR(image);
        } catch (IOException e) {
            System.err.println("Fehler beim Lesen des Bildes: " + e.getMessage());
        } catch (TesseractException e) {
            System.err.println("Fehler bei der OCR-Verarbeitung: " + e.getMessage());
        }
        return null;
    }

    public Invoice extractData(File file) throws TesseractException {
        String text = extractText(file);
        Invoice invoice = parseInvoiceFromText(text);
        invoice.setText(text);
        return invoice;
    }

    private Invoice parseInvoiceFromText(String text) { //AI generated (ChatGPT)
        Invoice invoice = new Invoice();

        Pattern amountPattern = Pattern.compile(
                "(?:EUR\\s*|€\\s*|)(\\d+[.,]\\d{2})(?:\\s*EUR|\\s*€)?",
                Pattern.CASE_INSENSITIVE
        );

        Matcher amountMatcher = amountPattern.matcher(text);
        if (amountMatcher.find()) {
            String amountString = amountMatcher.group(1).replace(",", ".");
            invoice.setAmount((float) Double.parseDouble(amountString));
        }

        // Beispiel: Datum erkennen (z.B. "01.04.2025")
        Pattern datePattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})");
        Matcher dateMatcher = datePattern.matcher(text);
        if (dateMatcher.find()) {
            String dateString = dateMatcher.group(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            invoice.setDate(LocalDate.parse(dateString, formatter));
        }

        InvoiceCategory category = categoryAnalyzer.getCategory(text);
        invoice.setCategory(category);
        if (invoice.getCategory() == InvoiceCategory.UNDETECTABLE) invoice.setFlag(true);
        return invoice;
    }
}
