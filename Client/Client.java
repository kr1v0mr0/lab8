package lab5.Client;

import lab5.Client.Managers.NetworkManager;
import lab5.Client.tools.Runner;
import lab5.Common.Commands.CommanfTypes;
import lab5.Common.Managers.Ask;
import lab5.Common.Tools.Consolka;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Client {
    public static void main(String[] args) throws Ask.AskBreak, IOException {
        Consolka console = new Consolka();
        NetworkManager networkManager = new NetworkManager(8000);

        while (!networkManager.init(args)) {}
        Map<CommanfTypes,String[]> commands = new HashMap<>();
        commands.put(CommanfTypes.Clear, new String[]{"clear", "очистить коллекцию"});
        commands.put(CommanfTypes.Exit, new String[]{"exit", "завершить программу"});
        commands.put(CommanfTypes.Help,new String[]{"help", "вывести справку по доступным командам"});
        commands.put(CommanfTypes.Info,new String[]{"info", "вывести информацию о коллекции"});
        commands.put(CommanfTypes.Show,new String[]{"show", "вывести в стандартный поток вывода все элементы коллекции в строковом представлении"});
        commands.put(CommanfTypes.Update,new String[]{"update <ID> {element}", "обновить значение элемента коллекции по ID"});
        commands.put(CommanfTypes.Insert, new String[]{"insert null {element}", "добавить новый элемент с заданным ключом"});
        commands.put(CommanfTypes.RemoveKey, new String[]{"removeKey null", "удалить элемент из коллекции по его ключу"});
        commands.put(CommanfTypes.Save, new String[]{"save", "сохранить коллекцию в файл"});
        commands.put(CommanfTypes.History, new String[]{"history", "вывести последние 8 команд (без их аргументов)"});
        commands.put(CommanfTypes.ReplaceIfGreater, new String[]{"replaceIfGreater null {element}", "заменить значение по ключу, если новое значение больше старого"});
        commands.put(CommanfTypes.RemoveLowerKey, new String[]{"removeLowerKey null", "удалить из коллекции все элементы, ключ которых меньше, чем заданный"});
        commands.put(CommanfTypes.CountLessThanGenre, new String[]{"countLessThanGenre genre", "вывести количество элементов, значение поля genre которых меньше заданного"});
        commands.put(CommanfTypes.CountGreaterThanNumberOfParticipants, new String[]{"countGreaterThanNumberOfParticipants numberOfParticipants", "вывести количество элементов, значение поля numberOfParticipants которых больше заданного"});
        commands.put(CommanfTypes.PrintUniqueStudio, new String[]{"printUniqueStudio", "вывести уникальные значения поля studio всех элементов в коллекции"});

        new Runner(networkManager,console,commands).init();

    }
}
