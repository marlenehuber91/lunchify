package backend.model;

public class FlaggedUser {
    private int userId;
    private int no_flaggs;
    private boolean permanent_flag;

    public FlaggedUser(int userId) { //AI generated
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getNoFlaggs() {
        return no_flaggs;
    }

    public void setNoFlaggs(int no_flaggs) {
        this.no_flaggs = no_flaggs;
    }

    public boolean isPermanentFlag() {
        return permanent_flag;
    }

    public void setPermanentFlag(boolean permanent_flag) {
        this.permanent_flag = permanent_flag;
    }
}



