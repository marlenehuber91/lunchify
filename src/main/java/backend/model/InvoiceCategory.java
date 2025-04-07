package backend.model;

//AI generated (Copilot) --> changed by the team

public enum InvoiceCategory {
	RESTAURANT {
	    @Override
	    public float calculateReimbursement(float amount, float limit) {
	        return (amount > limit) ? limit : amount;
	    }
	},
	SUPERMARKET {
	    @Override
	    public float calculateReimbursement(float amount, float limit) {
	        return (amount > limit) ? limit : amount;
	    }
	};
	
	public abstract float calculateReimbursement(float amount, float limit);
}