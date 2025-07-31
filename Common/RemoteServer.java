package lab5.Common;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteServer extends Remote {
    void registerClient(String var1, UpdateCollection var2) throws RemoteException;

    void notifyAllClients(String var1, String var2) throws RemoteException;
}

