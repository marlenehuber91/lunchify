package backend.logic;

import java.io.File;

import backend.model.Invoice;
import backend.model.InvoiceCategory;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.ITesseract;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.pdfbox.pdmodel.PDDocument;
import static org.apache.pdfbox.Loader.loadPDF;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.rendering.ImageType;

/**
 * Service class for performing Optical Character Recognition (OCR) on PDF and image files.
 * Extracts raw text, invoice amount, invoice date, and invoice category.
 */
public class OCRService {
    private final ITesseract tesseract;
    private final CategoryAnalyzer categoryAnalyzer = new CategoryAnalyzer();

    /**
     * Initializes the Tesseract OCR engine with the specified data path and languages.
     */
    public OCRService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/tessdata");
        tesseract.setLanguage("deu+eng");
    }

    /**
     * Extracts the text content from a PDF or image file using OCR.
     *
     * @param file the file to process
     * @return the extracted text
     * @throws IOException           if the file cannot be read
     * @throws TesseractException   if OCR fails
     */
    public String extractText(File file) throws IOException, TesseractException {
        if (isPDF(file)) {
            return extractTextFromPDF(file);
        } else {
            return extractTextFromImage(file);
        }
    }

    /**
     * Checks whether the given file is a PDF.
     *
     * @param file the file to check
     * @return true if it's a PDF, false otherwise
     */
    private boolean isPDF(File file) {
        return file.getName().toLowerCase().endsWith(".pdf");
    }

    /**
     * Extracts text from a PDF file by rendering each page and running OCR on it.
     *
     * @param pdfFile the PDF file
     * @return the extracted text
     * @throws IOException         if the PDF cannot be read
     * @throws TesseractException if OCR fails
     */
    private String extractTextFromPDF(File pdfFile) throws IOException, TesseractException {
        StringBuilder result = new StringBuilder();
        try (PDDocument document = loadPDF(pdfFile)) {
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            for (int page = 0; page < document.getNumberOfPages(); page++) {
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 300, ImageType.RGB); // 300 DPI für bessere OCR
                String pageText = tesseract.doOCR(image);
                result.append(pageText).append("\n");
            }
        }
        return result.toString();
    }

    /**
     * Extracts text from an image file (png, jpg, jpeg) using OCR.
     *
     * @param imageFile the image file
     * @return the extracted text
     * @throws IOException         if the image cannot be read
     * @throws TesseractException if OCR fails
     */
    private String extractTextFromImage(File imageFile) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Could not read image file: " + imageFile.getName());
        }
        return tesseract.doOCR(image);
    }

    /**
     * Extracts invoice data (text, amount, date, category) from a file.
     *
     * @param file the file to process
     * @return the extracted invoice object
     * @throws IOException         if file reading fails
     * @throws TesseractException if OCR fails
     */
    public Invoice extractData(File file) throws TesseractException, IOException {
        String text = extractText(file);
        Invoice invoice = parseInvoiceFromText(text);
        invoice.setText(text);
        return invoice;
    }

    /**
     * Parses invoice information (amount, date, category) from OCR text.
     *
     * @param text the raw OCR text
     * @return an Invoice object containing the parsed data
     */
    private Invoice parseInvoiceFromText(String text) { //Amount detection coded with AI assistance (not AI only)
        Invoice invoice = new Invoice();

        String[] lines = text.split("\\r?\\n");
        Float amount = null;

        // 1. Schritt: Suche nach "bezahlt"
        for (String line : lines) {
            String lower = line.toLowerCase();
            if (lower.contains("bezahlt") && !line.matches(".*\\d{2}\\.\\d{2}\\.\\d{4}.*")) {
                amount = extractAmountFromLine(line);
                if (amount != null) break; // Falls gefunden, abbrechen
            }
        }

        // 2. Schritt: Falls "bezahlt" nicht gefunden wurde, nimm "summe"
        if (amount == null) {
            for (String line : lines) {
                String lower = line.toLowerCase();
                if (lower.contains("summe") && !line.matches(".*\\d{2}\\.\\d{2}\\.\\d{4}.*")) {
                    amount = extractAmountFromLine(line);
                }
            }
        }

        // 3. Schritt: Fallback – höchsten Betrag im gesamten Text nehmen
        if (amount == null) {
            float maxAmount = 0f;
            Pattern fallbackPattern = Pattern.compile("(\\d+[.,]\\d{2})");
            for (String line : lines) {
                if (line.matches(".*\\d{2}\\.\\d{2}\\.\\d{4}.*")) continue; // Datumszeile überspringen
                Matcher matcher = fallbackPattern.matcher(line);
                while (matcher.find()) {
                    try {
                        float val = Float.parseFloat(matcher.group(1).replace(",", "."));
                        if (val > maxAmount) maxAmount = val;
                    } catch (NumberFormatException e) {
                        // Ignorieren
                    }
                }
            }
            if (maxAmount > 0f) amount = maxAmount;
        }

        if (amount != null) {
            invoice.setAmount(amount);
        }

        Pattern datePattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4})");
        Matcher dateMatcher = datePattern.matcher(text);
        if (dateMatcher.find()) {
            String dateString = dateMatcher.group(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            try {
                LocalDate parsedDate = LocalDate.parse(dateString, formatter);
                invoice.setDate(parsedDate);
            } catch (DateTimeParseException e) {
                e.printStackTrace();
            }
        }

        InvoiceCategory category = categoryAnalyzer.getCategory(text);
        invoice.setCategory(category);
        if (invoice.getCategory() == InvoiceCategory.UNDETECTABLE) invoice.setFlag(true);

        return invoice;
    }

    /**
     * Extracts the last amount (e.g. 12.99 or 3,50) found in a line of text.
     *
     * @param line the text line to scan
     * @return the parsed amount or null if none found
     */
    private Float extractAmountFromLine(String line) {
        Pattern pattern = Pattern.compile("(\\d+[.,]\\d{2})");
        Matcher matcher = pattern.matcher(line);
        Float lastValue = null;
        while (matcher.find()) {
            try {
                lastValue = Float.parseFloat(matcher.group(1).replace(",", "."));
            } catch (NumberFormatException e) {
            }
        }
        return lastValue;
    }


    /**
     * Helper class to determine invoice categories based on keyword matching.
     */
    private static class CategoryAnalyzer {
        private static final Set<String> SUPERMARKT_KEYWORDS = Set.of(
                "billa", "spar", "hofer", "aldi", "rewe", "edeka", "lidl", "penny", "kaufland", "tegut", "migros", "coop"
        );
        private static final Set<String> RESTAURANT_KEYWORDS = Set.of(
                "restaurant", "café", "bar", "bistro", "gaststätte", "tisch nr.", "bedienung", "speisen", "getränke", "trinkgeld"
        );

        private static InvoiceCategory getCategory(String ocrText) {
            if (ocrText == null || ocrText.isBlank()) {
                return null;
            }
            String lowerText = ocrText.toLowerCase(Locale.ROOT);

            for (String keyword : SUPERMARKT_KEYWORDS) {
                if (lowerText.contains(keyword)) {
                    return InvoiceCategory.SUPERMARKET;
                }
            }
            for (String keyword : RESTAURANT_KEYWORDS) {
                if (lowerText.contains(keyword)) {
                    return InvoiceCategory.RESTAURANT;
                }
            }
            return InvoiceCategory.UNDETECTABLE; //if undetectale -> Flag it!
        }
    }

        //for tests
    public ITesseract getTesseract() {
        return tesseract;
    }
}
