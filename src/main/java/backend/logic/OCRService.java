package backend.logic;

import java.io.File;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class OCRService {

    private final ITesseract tesseract;

    public OCRService() {
        tesseract = new Tesseract();
        tesseract.setDatapath("C:\\teaching-2025.ss.prse.braeuer.team4\\src\\main\\resources\\tessdata");      // z. B. "src/main/resources/tessdata"
        tesseract.setLanguage("deu+eng");
    }

    public String extractText(File imageFile) {
        try {
            BufferedImage image = ImageIO.read(imageFile);  // unterstützt PNG, JPG etc.
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
}
