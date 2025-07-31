package lab5.Server;

//import org.example.lab.both.RemoteServer;
//import org.example.lab.both.UpdateCollection;

import lab5.Common.UpdateCollection;
import lab5.Common.RemoteServer;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class RemoteControll extends UnicastRemoteObject implements RemoteServer {
    private final Map<String, UpdateCollection> clients = new ConcurrentHashMap<>();

    public RemoteControll() throws RemoteException {
        super();
    }

    @Override
    public synchronized void registerClient(String clientName, UpdateCollection client)
            throws RemoteException {
        clients.put(clientName, client);
        System.out.println("Клиент " + clientName + " зарегистрирован");
    }

    @Override
    public void notifyAllClients(String name, String collection) throws RemoteException {
        UpdateCollection client = clients.get(name);
        if(client!=null){
            if(!collection.isEmpty()){
                try{
                    client.updateCollection(collection);}
                catch (Exception e){

                }
            }
        }
        else{
            clients.remove(name);
        }
    }
}

