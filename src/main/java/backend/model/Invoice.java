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


    public int getId() {
        return id;
    }


    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public InvoiceCategory getCategory() {
        return category;
    }

    public void setCategory(InvoiceCategory category) {
        this.category = category;
    }

    public InvoiceState getStatus() {
        return status;
    }

    public void setStatus(InvoiceState status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public LocalDate getDate() {
    	return this.date;
    }
    
    public String toString() {
    	return this.user.getName() +", " + this.date + ", " + category;
    }
    public InvoiceCategory getCategory() {
    	return this.category;
    }
}
