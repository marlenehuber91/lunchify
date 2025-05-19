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

public class OCRService {

    private final ITesseract tesseract;
    private final CategoryAnalyzer categoryAnalyzer = new CategoryAnalyzer();


    public OCRService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/tessdata");
        tesseract.setLanguage("deu+eng");
    }


    public String extractText(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);
            if (image == null) {
                return null;
            }
            return tesseract.doOCR(image);
        } catch (IOException e) {
        	e.getStackTrace();
        } catch (TesseractException e) {
        	e.getStackTrace();
        }
        return null;
    }

    public Invoice extractData(File file) throws TesseractException {
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
                System.err.println("Warnung: Ungültiges Datum erkannt: " + dateString);
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
