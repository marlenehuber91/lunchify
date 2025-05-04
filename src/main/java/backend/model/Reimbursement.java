package backend.model;
import java.time.LocalDate;
import java.util.Date;

public class Reimbursement {
    private int id; //created automatically by database
    private Invoice invoice;
    private float approvedAmount;
    private Date processedDate;
    private ReimbursementState state;
    
    
    public Reimbursement() {
    	
    }
    
    public Reimbursement(Invoice invoice, float approvedAmount, Date processedDate) {
        this.invoice = invoice;
        this.approvedAmount = approvedAmount;
        this.processedDate = processedDate;
        this.state = ReimbursementState.PENDING;
    }

    public int getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public float getApprovedAmount() {
        return approvedAmount;
    }

    public Date getProcessedDate() {
        return processedDate;
    }
    
    
    public void setId (int id) {
    	this.id=id;
    }
    
	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}
    
	public void setApprovedAmount(float amount) {
		this.approvedAmount = amount;
	}
	
	public void setProcessedDate (Date date) {
		this.processedDate = date;
	}

	public ReimbursementState getStatus() {
		return this.state;
	}
	
	public void setStatus(ReimbursementState state) {
		this.state = state;
	}
	
	//zum testen
	public String toString() {
			String s = "ID: " + this.id + " Amount: " + this.approvedAmount + "Status" + this.state;
			s += "InvoiceKategorie: " + invoice.getCategory();
			s += "InvoiceDatum:" + invoice.getDate();
			s += "Rechnungsbetrag:" + invoice.getAmount();
			
			return s;
	}

    public boolean isReimbursementEditable() {
        return this.getInvoice().getDate().getMonthValue() == LocalDate.now().getMonthValue();
    }
}
