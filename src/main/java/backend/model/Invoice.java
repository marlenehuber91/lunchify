package backend.model;
import java.io.File;
import java.time.LocalDate;
import java.util.Date;


public class Invoice {
    private int id;
    private LocalDate date;
    private float amount;
    private InvoiceCategory category;
    private InvoiceState status;
    private User user;
    private File file;
    
    
    public Invoice (LocalDate date, float amount, InvoiceCategory category, InvoiceState status,  File file, User user) {
    	this.date=date;
    	this.amount=amount;
    	this.category=category;
    	this.status=status;
    	this.file=file;
    	this.user = user;
    }
    public Invoice() {
    	
    }

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
   
    public LocalDate getDate() {
    	return this.date;
    }
    
     public float getAmount() {
    	return this.amount;
    }
     
    public InvoiceCategory getCategory() {
    	return this.category;
    }
    
    public User getUser() {
    	return this.user;
    }
    
    public InvoiceState getState() {
    	return this.status;
    }
    
    public File getFile() {
		return file;
	}
    public void setId(int id) {
    	this.id=id;
    }
    
    public void setAmount(float amount) {
    	this.amount=amount;
    }
    
    public void setCategory(InvoiceCategory cat) {
    	this.category=cat;
    }
    
    public void setStatus(InvoiceState status) {
    	this.status=status;
    }
    public void setDate(LocalDate date) {
    	this.date=date;
    }
	
	public void setFile(File file) {
		this.file= file;
	}
	
}
