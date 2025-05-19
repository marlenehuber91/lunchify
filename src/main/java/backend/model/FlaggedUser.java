package backend.model;

public class FlaggedUser {
    private int userId;
    private int noFlaggs;
    private boolean permanentFlag;
    private String userName;

    public FlaggedUser(int userId) { //AI generated
        this.userId = userId; 
        this.permanentFlag = false;
    }

    public String getUserName() {
        return userName;
    }

    public String setUserName(String userName) {
        return this.userName = userName;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getNoFlaggs() {
        return noFlaggs;
    }

    public void setNoFlaggs(int noFlaggs) {
        this.noFlaggs = noFlaggs;
    }

    public boolean isPermanentFlag() {
        return permanentFlag;
    }

    public void setPermanentFlag(boolean permanentFlag) {
        this.permanentFlag = permanentFlag;
    }
}



