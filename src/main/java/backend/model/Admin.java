package backend.model;

public class Admin extends User {

    public Admin(String name, String email, String password, UserRole role, UserState state) {
        super(name, email, password, role, state);
    }


}