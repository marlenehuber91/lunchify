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
	},

	UNDETECTABLE {
		@Override
		public float calculateReimbursement(float amount, float limit) {
			return 0;
		} //amount needs to be altered manually if no category can be detected
	};
	
	public abstract float calculateReimbursement(float amount, float limit);
}