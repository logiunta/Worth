package Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerTcp {
    private int serverPort = 51150;
    public ServerSocket serverSocket;
    private ThreadPoolExecutor threadPool;
    private UsersDatabase userDb;
    private ProjectsDatabase projectsDb;
    private RmiServerNotifyImpl notify;
    private MultiGenerator multiGenerator;

    public ServerTcp(UsersDatabase userDb, RmiServerNotifyImpl notify, ProjectsDatabase projectsDb) throws IOException {
        serverSocket = new ServerSocket();
        this.projectsDb = projectsDb;
        this.userDb = userDb;
        this.notify = notify;
        multiGenerator =  Storage.restoreLastMultiAddress();

    }

    public void start() {
        try {
            serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), serverPort));
            threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();

        } catch (IOException e) {
            e.printStackTrace();
        }


        while (true) {
            try {
                Socket client = serverSocket.accept();
                System.out.println("Nuovo client connesso al sistema");
                OperationHandler handler = new OperationHandler(userDb, client, notify,multiGenerator,projectsDb);
                threadPool.execute(handler);

            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }



    }





