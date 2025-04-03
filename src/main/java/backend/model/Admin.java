package backend.model;
import java.util.List;
import java.util.Map;

public class Admin extends User {

    public Admin(String name, String email, String password, UserRole role, UserState state) {
        super(name, email, password, role, state);
    }


}