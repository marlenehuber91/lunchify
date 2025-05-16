package backend.model;

import java.time.LocalDate;

public class OCR {

    private static int id;
    private static LocalDate date;
    private static float amount;
    private static InvoiceCategory category;
    private static int userId;
    private static int referenceId;


    // Static Setter und Getter für 'id'
    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        OCR.id = id;
    }

    // Static Setter und Getter für 'date'
    public static LocalDate getDate() {
        return date;
    }

    public static void setDate(LocalDate date) {
        OCR.date = date;
    }

    // Static Setter und Getter für 'amount'
    public static float getAmount() {
        return amount;
    }

    public static void setAmount(float amount) {
        OCR.amount = amount;
    }

    // Static Setter und Getter für 'category'
    public static InvoiceCategory getCategory() {
        return category;
    }

    public static void setCategory(InvoiceCategory category) {
        OCR.category = category;
    }

    // Static Setter und Getter für 'userId'
    public static int getUserId() {
        return userId;
    }

    public static void setUserId(int userId) {
        OCR.userId = userId;
    }

    // Static Setter und Getter für 'referenceId'
    public static int getReferenceId() {
        return referenceId;
    }

    public static void setReferenceId(int referenceId) {
        OCR.referenceId = referenceId;
    }

    // Optional: Methode zur Rückgabe von OCR basierend auf userId
    public static OCR getBy(int userId) {
        // Logik hinzufügen, falls erforderlich
        return new OCR(); // Beispiel-Rückgabe
    }
}