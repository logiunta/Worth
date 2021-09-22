package Common;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiServerNotifyInterface extends Remote {

    public void signUpForCallBack(RmiClientNotifyInterface clientInt, String idClient) throws RemoteException;
    public void signOutFromCallBack(RmiClientNotifyInterface clientInts) throws RemoteException;


}
