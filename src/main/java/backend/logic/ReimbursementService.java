package backend.logic;


import backend.model.InvoiceCategory;
import backend.model.User;

public class ReimbursementService {
	private User user;
	private float reimbursementAmount;
	private float supermarketLimit=2.5f;
    private float restaurantLimit = 3.0f;
    
    public ReimbursementService() {
    	
    }
    
    public ReimbursementService(User user) {
    	this.user=user;
    }
    
    public float getReimbursementAmount() {
    	return this.reimbursementAmount;
    }
    
    public float getLimit(InvoiceCategory category) {
    	if(category== InvoiceCategory.RESTAURANT) {
    		return restaurantLimit;
    	}
    	else return supermarketLimit;
    }
    
    public void setReimbursementAmount(float amount) {
    	this.reimbursementAmount=amount;
    }
    public void setLimit(InvoiceCategory category, float amount) {
    	if(category== InvoiceCategory.RESTAURANT) {
    		this.restaurantLimit=amount;
    	}
    	else this.supermarketLimit=amount;
    }
    
    
}