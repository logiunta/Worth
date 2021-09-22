package Common;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

public interface RmiClientNotifyInterface extends Remote {

    public void notifyRegister(ArrayList<UserStatus> clientsDb, boolean sendNotif, String registeredUsers) throws RemoteException;
    public void notifyStatusOn(ArrayList<UserStatus> clientsDb, boolean sendNotif, String userOn) throws RemoteException;
    public void notifyStatusOff(ArrayList<UserStatus> clientsDb, String userOff) throws RemoteException;
    public void setIdClient(String idClient) throws RemoteException;
    public String getIdClient() throws RemoteException;
    public void notifyMulticastInfo(Project project) throws RemoteException;
    public void notifyProjectRemoved(String projectName,String userLoggedIn) throws RemoteException;



}
