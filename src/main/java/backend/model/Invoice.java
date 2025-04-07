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

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public int getId() {
        return id;
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
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setStatus(InvoiceState status) {
    	this.status=status;
    }
	
	public void setFile(File file) {
		this.file= file;
	}
	
	public void setDate(LocalDate date) {
		this.date=date; 
	}
}
