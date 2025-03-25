package backend.model;
import java.io.File;
import java.util.Date;


public class Invoice {
    private int id;
    private Date date;
    private float amount;
    private InvoiceCategory category;
    private InvoiceState status;
    private User user;
    private File file;

    // Methode zum Hochladen eines Bildes
    public void uploadImage(File image) {
        this.file = image;
        // Logik zum Hochladen kann hier hinzugefügt werden
    }

    // Methode zur Berechnung der Rückerstattung
    public float calculateReimbursement() {
        // Beispielberechnung (kann angepasst werden)
        return this.amount * 0.8f; // 80 % Rückerstattung
    }
}
