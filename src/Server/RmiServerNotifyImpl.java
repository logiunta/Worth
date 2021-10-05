package Server;

import Common.Project;
import Common.RmiClientNotifyInterface;
import Common.RmiServerNotifyInterface;
import Common.UserStatus;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class RmiServerNotifyImpl extends RemoteObject implements RmiServerNotifyInterface {
    private final HashMap<String, RmiClientNotifyInterface> clients;


    public RmiServerNotifyImpl() throws RemoteException {
        super();
        clients = new HashMap<>();

    }

    @Override
    public synchronized void signUpForCallBack(RmiClientNotifyInterface callBackClient, String idClient) throws RemoteException {
        if (!clients.containsKey(idClient)) {
            callBackClient.setIdClient(idClient); //associo il nome utente al nome del client per la ricezione di callback
            clients.put(idClient, callBackClient);
            System.out.println("Utente " + idClient + " aggiunto al servizio di notifica");

        } else {
            System.out.println("Utente " + idClient + " è gia iscritto al servizio di notifica");
        }
    }

    @Override
    public synchronized void signOutFromCallBack(RmiClientNotifyInterface callBackClient) throws RemoteException {
        String id = callBackClient.getIdClient();
        if (clients.remove(id) != null)
            System.out.println("Utente " + id + " rimosso dal servizio di notifica");
        else
            System.out.println("Errore: Impossibile rimuovere l'utente dal servizio di notifica");

    }

    public void update(ArrayList<UserStatus> clientsDb, String notify, String idClient) throws RemoteException {
        if (notify.equals("signUp"))
            doCallbacksRegister(clientsDb, idClient);
        else if(notify.equals("statusOn") || notify.equals("statusOff"))
            doCallbacksStatus(clientsDb, notify, idClient);
        else doCallBackInterrupt(clientsDb,idClient);

    }

    public synchronized void doCallbacksRegister(ArrayList<UserStatus> clientsDb, String idClient) throws RemoteException {
        boolean sendNotif; // lo uso per sapere a quale client inviare la callBack(tutti escluso il client che ha causato la notifica)
        Iterator<String> i = clients.keySet().iterator();
        while (i.hasNext()) {
            sendNotif = true;
            String id = i.next();
            if (id.equals(idClient))
                sendNotif = false;

            RmiClientNotifyInterface client = clients.get(id);
            client.notifyRegister(clientsDb, sendNotif, idClient);
        }
        System.out.println("Aggiornamento inviato");
    }

    public synchronized void doCallbacksStatus(ArrayList<UserStatus> clientsDb, String notify, String idClient) throws RemoteException {
        boolean sendNotif;
        Iterator<String> i = clients.keySet().iterator();

        if (notify.equals("statusOn")) {
            while (i.hasNext()) {
                sendNotif = true;
                String id = i.next();
                if (id.equals(idClient))
                    sendNotif = false;

                RmiClientNotifyInterface client = clients.get(id);
                client.notifyStatusOn(clientsDb, sendNotif, idClient);
            }

        } else {
            while (i.hasNext()) {
                String id = i.next();
                if (!id.equals(idClient)) {
                    RmiClientNotifyInterface client = clients.get(id);
                    client.notifyStatusOff(clientsDb, idClient);
                }

            }

        }
        System.out.println("Aggiornamento inviato");
    }

    public synchronized List<RmiClientNotifyInterface> getClients() {
        return new ArrayList<>(clients.values());
    }

    //nel caso in cui il client crashi, non avrà fatto signOutFromCallBack, deve farlo il server
    private synchronized void doCallBackInterrupt(ArrayList<UserStatus> clientsDb, String idClient) throws RemoteException {
        clients.remove(idClient);
        Iterator<String> iterator = clients.keySet().iterator();
        System.out.println("Utente " + idClient + " rimosso dal servizio di notifica");
        while (iterator.hasNext()) {
            String id = iterator.next();
            RmiClientNotifyInterface client = clients.get(id);
            client.notifyStatusOff(clientsDb, idClient);
        }
    }

    public synchronized void doProjectDeletedCallback(Project project,String userLogged) throws RemoteException{
        Iterator<String> iterator = clients.keySet().iterator();
        while(iterator.hasNext()){
            String client = iterator.next();
            if(project.getUsers().contains(client))
                clients.get(client).notifyProjectRemoved(project.getProjectName(),userLogged);
        }
    }
    public synchronized void doMulticastCallback(String user,Project project) throws RemoteException {
        if(clients.containsKey(user)){
            clients.get(user).notifyMulticastInfo(project);
        }

    }


}


