package backend.model;
import java.io.File;
import java.time.LocalDate;


public class Invoice {
    private int id;
    private LocalDate date;
    private float amount;
    private InvoiceCategory category;
    private User user;
    private File file;
    private boolean flag;
    private String text;



    public Invoice(LocalDate date, float amount, InvoiceCategory category,
                   File file, User user) {
        this.date = date;
        this.amount = amount;
        this.category = category;
        this.file = file;
        this.user = user;
        this.text = "";
        this.flag = false; //only true if needed
    }

    public Invoice() {

    }

    public int getId() {
        return id;
    }

    public LocalDate getDate() {
        return this.date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
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

    public File getFile() {
        return file;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setCategory(InvoiceCategory cat) {
        this.category = cat;
    }

    public void setUser(User user) {
        this.user = user;
    }
    
    public void setFile(File file) {
        this.file = file;
    }

    public boolean isFlagged() {
        return flag;
    }

    public void setFlag(boolean flagStatus) {
        this.flag = flagStatus;
    }

    public void setText(String text) {
    }

    public String getText() {
        return this.text;
    }
    
    public int getUserId() {
    	return this.user.getId();
    }
}
