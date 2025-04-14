package backend.logic;

import backend.model.InvoiceCategory;

import java.util.Locale;
import java.util.Set;

public class CategoryAnalyzer {
    private static final Set<String> SUPERMARKT_KEYWORDS = Set.of(
            "billa", "spar", "hofer", "aldi", "rewe", "edeka", "lidl", "penny", "kaufland", "tegut", "migros", "coop"
    );
    private static final Set<String> RESTAURANT_KEYWORDS = Set.of(
            "restaurant", "café", "bar", "bistro", "gaststätte", "tisch nr.", "bedienung", "speisen", "getränke", "trinkgeld"
    );

    public static InvoiceCategory getCategory(String ocrText) {
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

