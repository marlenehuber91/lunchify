package backend.exceptions;

public class ReimbursementException extends RuntimeException {
    public ReimbursementException(String message) {
        super(message);
    }

    public ReimbursementException(String message, Throwable cause) {
        super(message, cause);
    }
}
