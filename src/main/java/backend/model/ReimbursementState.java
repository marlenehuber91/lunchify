package backend.model;

public enum ReimbursementState {
    PENDING,       // Rechnung wurde hochgeladen, aber noch nicht verarbeitet
    APPROVED,      // Rechnung wurde geprüft und genehmigt
    REJECTED,      // Rechnung wurde abgelehnt
    FLAGGED;	   // Rechnung muss von einem Admin geprüft werden, da Anomalie erkannt wurde


    // Optional: Methode zur Beschreibung des Status
    public String getDescription() {
        switch (this) {
            case PENDING:
                return "Die Rechnung wartet auf Bearbeitung.";
            case APPROVED:
                return "Die Rechnung wurde genehmigt.";
            case REJECTED:
                return "Die Rechnung wurde abgelehnt.";
            case FLAGGED:
            	return "Die Rechnung muss von einem Admin geprüft werden";
            default:
                return "Unbekannter Status.";
        }
    }
}