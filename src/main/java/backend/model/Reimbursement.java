package backend.model;
import java.util.Date;

public class Reimbursement {
    private int id;
    private Invoice invoice;
    private float approvedAmount;
    private Date processedDate;

    public Reimbursement(int id, Invoice invoice, float approvedAmount, Date processedDate) {
        this.id = id;
        this.invoice = invoice;
        this.approvedAmount = approvedAmount;
        this.processedDate = processedDate;
    }

    public int getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public double getApprovedAmount() {
        return approvedAmount;
    }

    public Date getProcessedDate() {
        return processedDate;
    }

}
