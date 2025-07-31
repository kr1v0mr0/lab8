package lab5.Server.Commands;

import lab5.Common.Commands.Command;
import lab5.Common.Models.MusicBand;
import lab5.Common.Tools.Consolka;
import lab5.Common.Tools.ExecutionResponse;
import lab5.Server.Managers.CollectionManager;
/**
 * Команда 'replace_if_greater'. заменяет значение по ключу, если новое значение больше старого
 */
public class ReplaceIfGreater extends Command {
    private final CollectionManager collectionManager;
    private final Consolka consolka;
    public ReplaceIfGreater(Consolka consolka, CollectionManager collectionManager) {
        super("replace_if_greater", "заменить значение по количеству альбомов, если новое значение больше старого");
        this.consolka = consolka;
        this.collectionManager = collectionManager;
    }
    @Override
    public ExecutionResponse apply(String[] arguments, String username) {
        if (!arguments[1].isEmpty()) {
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        }
        Integer cnt = -1;
        try { cnt = Integer.parseInt(arguments[1].trim()); } catch (NumberFormatException e) { return new ExecutionResponse(false, "Количество альбомов не распознано"); }
        for (MusicBand e: collectionManager.getCollection().values()){
            if(e.getAlbumsCount()>cnt && collectionManager.getUsersElements().get(e.getId()).equals(collectionManager.getUser(username)))
            {
                ExecutionResponse ans = collectionManager.update(e);
                if(ans.exitCode()==false){ return new ExecutionResponse( false, "всё пошло не по плану"); }
                return new ExecutionResponse(true, "элемент успешно заменен");
            }
        }
        return null;
    }
}
