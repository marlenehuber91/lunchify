package lunchify;

//AI generated (Copilot)

public enum InvoiceCategory {
    RESTAURANT {
        @Override
        public float calculateReimbursement(float amount) {
            return (amount > 3.0f) ? 3.0f : amount;
        }
    },
    SUPERMARKET {
        @Override
        public float calculateReimbursement(float amount) {
            return (amount > 2.5f) ? 2.5f : amount;
        }
    };

    public abstract float calculateReimbursement(float amount);
}