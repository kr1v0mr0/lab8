package lab5.Server.Commands;

import lab5.Common.Commands.Command;
import lab5.Common.Managers.Ask;
import lab5.Common.Models.MusicBand;
import lab5.Common.Tools.Consolka;
import lab5.Common.Tools.ExecutionResponse;
import lab5.Server.Managers.CollectionManager;

/**
 * Команда 'update'. обновляет значение элемента коллекции, id которого равен заданному
 */
public class Update extends Command {

    private final Consolka consolka;
    private final CollectionManager collectionManager;

    public Update(Consolka consolka, CollectionManager collectionManager) {
        super("update <ID> {element}", "обновить значение элемента коллекции по ID");
        this.consolka = consolka;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String[] arguments, String username) {
        if (arguments[1].isEmpty())
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        Integer id = -1;
        try {
            id = Integer.parseInt(arguments[1].split("@")[0].trim());
        } catch (NumberFormatException e) {
            return new ExecutionResponse(false, "ID не распознан");
        }
        if(collectionManager.getUsersElements().get(id).equals(collectionManager.getUser(username))) {

            MusicBand old = collectionManager.byId(id);
            if (old == null || !collectionManager.getCollection().containsKey(old.getId())) {
                return new ExecutionResponse(false, "Не существующий ID");
            }
            MusicBand d = MusicBand.fromArray(arguments[1].split("@"));
            if (d != null && d.validate()) {
                collectionManager.update(d);
                collectionManager.update();
                return new ExecutionResponse(true, "Обновлено!");
            } else {
                return new ExecutionResponse(false, "Поля неверны");
            }
        }
        else{
            return new ExecutionResponse(true, "Вы не можете изменять этот элемент");
        }

    }
}