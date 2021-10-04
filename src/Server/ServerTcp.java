package Server;

import java.io.*;
import java.net.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ServerTcp {
    private int serverPort = 51150;
    public ServerSocket serverSocket;
    private ThreadPoolExecutor threadPool;
    private UsersDatabase userDb;
    private ProjectsDatabase projectsDb;
    private RmiServerNotifyImpl notify;
    private MultiGenerator multiGenerator;
    private ShutdownThread shutdownThread;


    public ServerTcp(UsersDatabase userDb, RmiServerNotifyImpl notify, ProjectsDatabase projectsDb) throws IOException {
        serverSocket = new ServerSocket();
        this.projectsDb = projectsDb;
        this.userDb = userDb;
        this.notify = notify;
        multiGenerator = Storage.restoreLastMultiAddress();
        shutdownThread = new ShutdownThread(serverSocket);

    }

    public void start() {
        try {
            serverSocket.bind(new InetSocketAddress(InetAddress.getLocalHost(), serverPort));
            threadPool = (ThreadPoolExecutor) Executors.newCachedThreadPool();
            new Thread(shutdownThread).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (true) {
                Socket client = serverSocket.accept();
                shutdownThread.addSocket(client);
                System.out.println("Nuovo client connesso al sistema");
                TcpHandler handler = new TcpHandler(userDb, client, notify, multiGenerator, projectsDb);
                threadPool.execute(handler);
            }
        } catch (IOException e) {
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(3000,TimeUnit.MILLISECONDS);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }
    }
}








