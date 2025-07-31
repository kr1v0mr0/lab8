package lab5.Server.Commands;

import lab5.Common.Commands.Command;
import lab5.Common.Tools.Consolka;
import lab5.Common.Tools.ExecutionResponse;
import lab5.Server.Managers.CollectionManager;

/**
 * Команда 'RemoveKey'.  удаляет элемент из коллекции по его ключу
 */
public class RemoveKey extends Command {
    private final CollectionManager collectionManager;
    private final Consolka consolka;
    public RemoveKey(Consolka consolka, CollectionManager collectionManager) {
        super("remove_key null", "удалить элемент из коллекции по его id");
        this.consolka = consolka;
        this.collectionManager = collectionManager;
    }
    @Override
    public ExecutionResponse apply(String[] arguments, String username) {
        if (arguments[1].isEmpty()) {

            return new ExecutionResponse(false, "Неправильное количество аргументов!\n" + "Использование: '" + getName() + "'");
        }
        Integer id = -1;
        //try { id = Integer.parseInt(arguments[1].trim()); } catch (NumberFormatException e) { return new ExecutionResponse(false, "ID не распознан"); }

            for( String e: arguments[1].split("/")){
                try{
                    id = Integer.parseInt(e);
                    if(collectionManager.getUsersElements().get(id).equals(collectionManager.getUser(username))) collectionManager.remove(id);
                }
                catch (NumberFormatException ex){

                }
            }
            return new ExecutionResponse(true, "Успешно удалены!");
        }
    }

