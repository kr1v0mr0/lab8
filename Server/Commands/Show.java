package lab5.Server.Commands;

import lab5.Common.Commands.Command;
import lab5.Common.Models.MusicBand;
import lab5.Common.Tools.Consolka;
import lab5.Common.Tools.ExecutionResponse;
import lab5.Server.Managers.CollectionManager;

import java.util.HashMap;

/**
 * Команда 'show'. выводит в стандартный поток вывода все элементы коллекции в строковом представлении
 */
public class Show extends Command {
    private final Consolka consolka;
    private final CollectionManager collectionManager;

    public Show(Consolka consolka, CollectionManager collectionManager) {
        super("show", "вывести в стандартный поток вывода все элементы коллекции в строковом представлении");
        this.consolka = consolka;
        this.collectionManager = collectionManager;
    }
    @Override
    public ExecutionResponse apply(String[] arguments, String username) {
        if (!arguments[1].isEmpty()) return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        StringBuilder sb= new StringBuilder();
        HashMap<Integer, Integer> usersElements = collectionManager.getUsersElements();
        for (MusicBand e: collectionManager.getCollection().values()){
            if(usersElements.get(e.getId()).equals(collectionManager.getUser(username))){
                sb.append("Your element:" + e.toString()+ "\n");
            }
            else{
                sb.append(e.toString()+"\n");
            }
        }
        return new ExecutionResponse(true, sb.toString());
    }
}
