package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class ShutdownThread implements Runnable{
    private final LinkedList<Socket> socketsList;
    private final ServerSocket serverSocket;


    public ShutdownThread(ServerSocket serverSocket){
        socketsList = new LinkedList<>();
        this.serverSocket = serverSocket;

    }

    public void addSocket(Socket socket){
        synchronized (socketsList) {
            socketsList.add(socket);
        }
    }

    @Override
    public void run() {
        String command;
        Scanner scanner = new Scanner(System.in);
        while(!(command = scanner.nextLine()).trim().equals("exit"));

        shutDown();

    }

    private void shutDown(){
        synchronized (socketsList) {
            for (Socket socket : socketsList) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
