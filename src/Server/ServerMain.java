package Server;

import Common.*;

import java.io.IOException;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Iterator;

public class ServerMain {
    private static final int rmiPort = 54500;
    private static final int rmi_callbackPort = 53780;
    private static RmiServerRegistrationImpl rmiServer;
    private static RmiServerInterfaceRegistration stubRegister;
    private static Registry r;
    private static RmiServerNotifyImpl rmiServerNotify;
    private static RmiServerNotifyInterface stubNotify;
    private static UsersDatabase usersDb;
    private static ProjectsDatabase projectsDb;


    public static void main(String[] args) {
        ServerTcp serverTcp;
        usersDb = new UsersDatabase();
        projectsDb = new ProjectsDatabase();

        try {
            rmiServerNotify = new RmiServerNotifyImpl();
            stubNotify = (RmiServerNotifyInterface) UnicastRemoteObject.exportObject(rmiServerNotify, 0);
            LocateRegistry.createRegistry(rmi_callbackPort);
            LocateRegistry.getRegistry(rmi_callbackPort).bind("NotifyService", stubNotify);

            rmiServer = new RmiServerRegistrationImpl(usersDb, rmiServerNotify);
            stubRegister = (RmiServerInterfaceRegistration) UnicastRemoteObject.exportObject(rmiServer, 39000);
            LocateRegistry.createRegistry(rmiPort);
            r = LocateRegistry.getRegistry(rmiPort);
            r.bind("SignUpService", stubRegister);

            resetAllCallbacks();
            System.out.println("Avvio del server Worth. . .\n\n\n");
            System.out.println("Server avviato\nIn attesa di richieste");
            serverTcp = new ServerTcp(usersDb, rmiServerNotify,projectsDb);
            serverTcp.start();


        }
        catch (AlreadyBoundException | IOException e){
            e.printStackTrace();
        }

    }

    private static void resetAllCallbacks(){
        Iterator<RmiClientNotifyInterface> iterator = rmiServerNotify.getClients().iterator();
        while(iterator.hasNext()){
            RmiClientNotifyInterface client = iterator.next();
            rmiServerNotify.getClients().remove(client);
        }

    }








}


