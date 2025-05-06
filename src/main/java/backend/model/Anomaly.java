package backend.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;

public class Anomaly { //AI generated

    private int id;
    private LocalDateTime detectedAt;
    private int invoiceId;
    private int userId;
    private String userName; //getUserById
    private String invoiceDate;

    public Anomaly(int id, LocalDateTime detectedAt, int invoiceId, int userId, String userName, String invoiceDate) {
        this.id = id;
        this.detectedAt = detectedAt;
        this.invoiceId = invoiceId;
        this.userId = userId;
        this.userName = userName;
        this.invoiceDate = invoiceDate;
    }

    // Getter und Setter
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDetectedAt() {
        return detectedAt;
    }

    public void setDetectedAt(LocalDateTime detectedAt) {
        this.detectedAt = detectedAt;
    }

    public int getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(int invoiceId) {
        this.invoiceId = invoiceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

}
