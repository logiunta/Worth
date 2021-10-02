package Client;

import Common.MulticastConnection;
import Common.Project;
import Common.RmiClientNotifyInterface;
import Common.UserStatus;

import java.io.IOException;
import java.net.MulticastSocket;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;

public class RmiClientNotifyImpl extends RemoteObject implements RmiClientNotifyInterface {
    private String idClient;
    private LocalDb clientUsersDb;
    private MulticastConnection myChats;

    public RmiClientNotifyImpl(LocalDb clientUsersDb,MulticastConnection myChats) {
        super();
        this.clientUsersDb = clientUsersDb;
        this.idClient = null;
        this.myChats = myChats;

    }

    @Override
    public void notifyRegister(ArrayList<UserStatus> clientsDb, boolean sendNotif, String registeredUsers) throws RemoteException{
        clientUsersDb.fill(clientsDb);
        if(sendNotif)
            System.out.print("\n< Notifica dal sistema: " +registeredUsers+ " si è registrato a Worth\n> ");

    }
    @Override
    public void notifyStatusOn(ArrayList<UserStatus> clientsDb, boolean sendNotif, String userOn) throws RemoteException {
        clientUsersDb.fill(clientsDb);
        if(sendNotif)
            System.out.print("\n< Notifica dal sistema: " +userOn + " ha cambiato il suo stato in: online\n> ");


    }
    @Override
    public void notifyStatusOff(ArrayList<UserStatus> clientsDb, String userOff) throws RemoteException{
        clientUsersDb.fill(clientsDb);
        System.out.print("\n< Notifica dal sistema: " +userOff + " ha cambiato il suo stato in: offline\n> ");
    }

    @Override
    public void setIdClient(String idClient) throws RemoteException {
        this.idClient = idClient;
    }

    @Override
    public String getIdClient() throws RemoteException{
        return idClient;
    }

    //usato dal server per comunicare al client le info affinchè il client faccia join sulla chat in cui è stato aggiunto
    @Override
    public void notifyMulticastInfo(Project project) throws RemoteException{
        String ipMulticast = project.getMultiAddress();
        String projectName = project.getProjectName();

        myChats.joinChat(projectName,ipMulticast);
        System.out.print("\n< Notifica dal sistema: sei stato aggiunto al progetto " + projectName +", ora puoi parteciparne alla chat\n>");

    }

    @Override
    public void notifyProjectRemoved(String projectName,String userLoggedIn) throws RemoteException {
        myChats.leaveChat(projectName);
        if(!userLoggedIn.equals(idClient)) //non lo notifico all'utente che ha cancellato il progetto
            System.out.print("\n< Notifica dal sistema: il progetto " + projectName + " è stato eliminato da un membro del gruppo\n>");

    }


}
