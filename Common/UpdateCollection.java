package lab5.Common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface UpdateCollection extends Remote {
    void updateCollection(String var1) throws RemoteException;
}
