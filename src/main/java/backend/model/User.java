package backend.model;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Objects;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class User {
    private int id;
    @XmlElement
    private String name;
    @XmlElement
    private String email;
    private String password;
    private UserRole role;
    private UserState state;
    
    public User() {};

    public User(String name, String email, String password, UserRole role, UserState state) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.state = state;
    }
    //never use to add new user to database! only for local user copy
    public User(int id, String name, String email, UserRole role, UserState state) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    };

    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPassword() {
    	return this.password;
    }
    

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public UserState getState() {
        return state;
    }

    public void setState(UserState state) {
        this.state = state;
    }


    @Override
    public String toString() {
        return id + " - " + name;
    }

    public boolean isNameValid() {
        return name != null && !name.isBlank();
    }

    public boolean isEmailValid() {
        return email != null && email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }
    public boolean isPasswordValid() {
        return true;
        //TODO: rules for good password
    }
    public boolean isRoleValid() {
        return role != null;
    }
    public boolean isStateValid() {
        return state != null;
    }
    public String hashPassword() {
        return BCrypt.hashpw(this.password, BCrypt.gensalt());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return id == user.id; // Nur die ID entscheidet über Gleichheit
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}