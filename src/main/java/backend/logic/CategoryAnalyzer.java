package backend.logic;

import java.util.Locale;
import java.util.Set;

public class CategoryAnalyzer {
    private static final Set<String> SUPERMARKT_KEYWORDS = Set.of(
            "billa", "spar", "hofer", "aldi", "rewe", "edeka", "lidl", "penny", "kaufland", "tegut", "migros", "coop"
    );

    public static String fromOCRText(String ocrText) {
        String lowerText = ocrText.toLowerCase(Locale.ROOT);

        for (String keyword : SUPERMARKT_KEYWORDS) {
            if (lowerText.contains(keyword)) {
                return "Supermarkt";
            }
        }
        return "Restaurant";
    }
}
}
