package backend.logic;
import java.io.File;


public class UploadService {

    private final OCRService ocrService;

    public UploadService(String tessDataPath) {
        ocrService = new OCRService();
    }

    public String processInvoiceImage(File imageFile) {
        return ocrService.extractText(imageFile);
    }


}




