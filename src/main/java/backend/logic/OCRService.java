package backend.logic;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import java.io.File;

public class OCRService {

    private final Tesseract tesseract;

    public OCRService(String tessDataPath) {
        tesseract = new Tesseract();
        tesseract.setDatapath(tessDataPath);
    }

    public String extractText(File imageFile) {
        try {
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            System.err.println("Fehler bei der OCR-Verarbeitung: " + e.getMessage());
            return null;
        }
    }
}