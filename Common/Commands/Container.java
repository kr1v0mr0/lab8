package lab5.Common.Commands;

import lab5.Common.Models.User;

import java.io.Serializable;

public class Container implements Serializable {
    private static final long serialVersionUID = 15L;
    private CommanfTypes commandType;
    private String args;
    private User user;

    public Container(User user){
        this.user = user;
    }

    public Container(CommanfTypes commandType, String args, User user) {
        this.commandType = commandType;
        this.args = args;
        this.user = user;
    }

    public CommanfTypes getCommandType() {
        return commandType;
    }

    public String getArgs() {
        return args;
    }

    public User getUser(){ return user; }


}