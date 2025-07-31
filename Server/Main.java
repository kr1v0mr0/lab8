package lab5.Server;

import lab5.Common.RemoteServer;
import lab5.Common.Tools.Consolka;
import lab5.Server.Commands.*;
import lab5.Server.Managers.CollectionManager;
import lab5.Server.Managers.CommandManager;
import lab5.Server.Managers.DBManager;
import lab5.Server.Managers.NetworkManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.rmi.Remote;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import static lab5.Server.Server.logger;

public class Main {
    //java -cp "Server.jar;gson-2.8.6.jar;log4j-api-2.24.3.jar;log4j-core-2.24.3.jar;both.jar" lab5.Server.Server
    public static void main(String[] args) {
        Configurator.initialize(null, "log4j2.xml");
        logger = LogManager.getLogger(Server.class);
        Consolka consolka = new Consolka();
        if (args.length == 0) {
            consolka.println("Введите имя загружаемого файла как аргумент командной строки");
            logger.error("Не введено название файла. Сервер не был запущен.");
            System.exit(1);
        }
        logger.info("Сервер успешно запущен!");
        DBManager dBManager = new DBManager(args[0], "jdbc:postgresql://localhost:5433/studs");
        CollectionManager collectionManager = new CollectionManager(dBManager);

        if (!collectionManager.init().exitCode()) {
            logger.error("Не удалось загрузить коллекцию.");
            System.exit(1);
        }

        NetworkManager networkManager = new NetworkManager(32432, 800);
        while (!networkManager.init()) {
            logger.debug("Попытка инициализации NetworkManager...");
        }
        logger.info("Менеджер сетевого взаимодействия инициализирован!");

        collectionManager.update();
        CommandManager commandManager = new CommandManager() {{
            register("info", new Info(consolka, collectionManager));
            register("show", new Show(consolka, collectionManager));
            register("insert", new Insert(consolka, collectionManager));
            register("update", new Update(consolka, collectionManager));
            register("removeKey", new RemoveKey(consolka, collectionManager));
            register("clear", new Clear(consolka, collectionManager));
            register("replaceIfGreater", new ReplaceIfGreater(consolka, collectionManager));
            register("removeLowerKey", new RemoveLowerKey(consolka, collectionManager));
            register("countLessThanGenre", new CountLessThanGenre(consolka, collectionManager));
            register("countGreaterThanNumberOfParticipants", new CountGreaterThanNumberOfParticipants(consolka, collectionManager));
            register("printUniqueStudio", new PrintUniqueStudio(consolka, collectionManager));
        }};
        History h = new History(consolka);
        h.setCommandManager(commandManager);
        commandManager.register("history", h);
        try{
            RemoteControll rm = new RemoteControll();
        }
        catch (Exception e){
            RemoteControll rm = null;
        }
        Server server = new Server(networkManager, commandManager, collectionManager);
        server.run();
    }
}
