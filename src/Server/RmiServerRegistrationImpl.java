package Server;

import Common.RmiServerInterfaceRegistration;

import Common.UserStatus;
import Exceptions.UserAlreadySignedInException;

import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.util.ArrayList;

public class RmiServerRegistrationImpl extends RemoteObject implements RmiServerInterfaceRegistration {
    private UsersDatabase usersDb;
    private RmiServerNotifyImpl notify;

    public RmiServerRegistrationImpl(UsersDatabase usersDb, RmiServerNotifyImpl notify) throws RemoteException {
        this.usersDb = usersDb;
        this.notify = notify;
    }


    @Override
    public synchronized String signUp(String[] myargs) throws RemoteException, UserAlreadySignedInException {
        String res = null;
        ArrayList<UserStatus> clientsDb = null;
        clientsDb = usersDb.addUser(myargs[1],myargs[2]);
        notify.update(clientsDb,"signUp", myargs[1]);
        res = ("L'utente " + myargs[1] + " è stato registrato a Worth correttamente");
        return res;

    }







}

