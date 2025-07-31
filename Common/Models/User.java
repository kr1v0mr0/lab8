package lab5.Common.Models;
import lab5.Common.Tools.Element;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 17L;
    private final String name;
    private final String password;

    public User(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public boolean validate() {
        return getName().length() < 40;
    }

    public User copy(int id) {
        return new User(getName(), getPassword());
    }


    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "User{" +
                ", name='" + name + '\'' +
                ", password='********'" +
                '}';
    }

}