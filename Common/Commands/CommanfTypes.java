package lab5.Common.Commands;

import java.io.Serializable;

public enum CommanfTypes implements Serializable {

    Help("help"),
    Info("info"),
    Show("show"),
    Insert("insert"),
    Update("update"),
    RemoveKey("removeKey"),
    Clear("clear"),
    Save("save"),
    Exit("exit"),
    History("history"),
    ReplaceIfGreater("replaceIfGreater"),
    RemoveLowerKey("removeLowerKey"),
    CountLessThanGenre("countLessThanGenre"),
    CountGreaterThanNumberOfParticipants("countGreaterThanNumberOfParticipants"),
    PrintUniqueStudio("printUniqueStudio");


    private String type;

    private CommanfTypes(String type) {
        this.type = type;
    }

    public String Type() {
        return type;
    }

    private static final long serialVersionUID = 14L;

    public static CommanfTypes getByString(String string) {
        try {

            return CommanfTypes.valueOf(string.toUpperCase().charAt(0) + string.substring(1));
        } catch (NullPointerException | IllegalArgumentException e) {
        }
        return null;
    }
}