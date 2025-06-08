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

public class OCRService {
    private final ITesseract tesseract;
    private final CategoryAnalyzer categoryAnalyzer = new CategoryAnalyzer();

    public OCRService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/tessdata");
        tesseract.setLanguage("deu+eng");
    }


    public String extractText(File file) throws IOException, TesseractException {
        if (isPDF(file)) {
            return extractTextFromPDF(file);
        } else {
            return extractTextFromImage(file);
        }
    }

    private boolean isPDF(File file) {
        return file.getName().toLowerCase().endsWith(".pdf");
    }

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

    private String extractTextFromImage(File imageFile) throws IOException, TesseractException {
        BufferedImage image = ImageIO.read(imageFile);
        if (image == null) {
            throw new IOException("Could not read image file: " + imageFile.getName());
        }
        return tesseract.doOCR(image);
    }

    public Invoice extractData(File file) throws TesseractException, IOException {
        String text = extractText(file);
        Invoice invoice = parseInvoiceFromText(text);
        invoice.setText(text);
        return invoice;
    }

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
