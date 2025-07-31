package lab5.Server;
//import lab5.Client.Commands.Exit;
import lab5.Common.Commands.Command;
import lab5.Common.Commands.Container;
import lab5.Common.Models.User;
import lab5.Common.RemoteServer;
import lab5.Common.Tools.Consolka;
import lab5.Common.Tools.ExecutionResponse;
import lab5.Server.Commands.*;
import lab5.Server.Managers.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;

import java.rmi.Naming;
import java.rmi.registry.LocateRegistry;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Server {
    private byte arr[] = new byte[5069];
    private int len = arr.length;
    public static Logger logger;
    private String[] userCommand = new String[2];
    private NetworkManager networkManager;
    private CommandManager commandManager;
    private CollectionManager collectionManager;
    private User user;
    private RemoteControll remoteControll;
    private ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1000);
    public Server(NetworkManager networkManager, CommandManager commandManager, CollectionManager collectionManager){
        this.networkManager=networkManager;
        this.commandManager=commandManager;
        this.collectionManager = collectionManager;
        //this.remoteControll = remoteControll;
    }

    public void launch(Container commandd){
        if (commandd!= null) {
            ExecutionResponse response;
            if( commandd.getCommandType()==null){
                response = collectionManager.regUser(commandd.getUser());
                //e.toStr()+@+"1/0"+"&"+e1
            }
            else {
                userCommand[0] = commandd.getCommandType().Type();
                userCommand[1] = commandd.getArgs();
                Command command = commandManager.getCommands().get(userCommand[0]);
                if (userCommand[0].equals("")) {
                    response = new ExecutionResponse(true, "");
                } else if (command == null) {
                    logger.warn("Неизвестная команда: {}", userCommand[0]);
                    response = new ExecutionResponse(false, "Команда '" + userCommand[0] + "' не найдена.");
                } else {
                    logger.debug("Выполнение команды: {}", userCommand[0]);
                    response = command.apply(userCommand, commandd.getUser().getName());
                    commandManager.addToHistory(userCommand[0]);
                }
            }

            logger.info("Команда '{}' обработана", userCommand[0]);// Создаем новый поток для отправки данных
            new Thread(() -> {
                try {
                    networkManager.sendData(NetworkManager.serializer(response));
                } catch (Exception e) {
                    // Обработка ошибок при отправке
                    e.printStackTrace();
                }
            }).start();
            logger.debug("Ответ серверу отправлен");
        }
    }
    public void run() {
        new Thread(this::startRMI).start();
        while (true) {
            try {
                lab5.Common.Commands.Container commandd = NetworkManager.deserialize(networkManager.receiveData(len));
                executor.submit(()-> {launch(commandd);});

            } catch (Exception e) {
                logger.error("Ошибка при обработке команды: ", e);
            }
        }

    }
    private void startRMI() {
        try {
            RemoteServer rmiService = new RemoteControll();
            LocateRegistry.createRegistry(1099);
            Naming.rebind("rmi://localhost:" + 1099 + "/RemoteService", rmiService);
            collectionManager.setRmiControll(rmiService);
            System.out.println("RMI Server started on port " + 1099);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}