package Common;

import Exceptions.ProjectAlreadyAddedException;


import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MulticastChats implements Runnable {
    private final int multiPort = 63000;
    private final HashMap<String,InetAddress> infoProjects;
    private MulticastSocket multicastSocket;
    private final HashMap<String,ArrayList<Message>> messaggesQueue; //associa ad ogni progetto la lista di messaggi
    private boolean isRunning = true;

    public MulticastChats() {
        infoProjects = new HashMap<>();
        messaggesQueue = new HashMap<>();
        try {
            multicastSocket = new MulticastSocket(multiPort);

        }
         catch (IOException e) {
        e.printStackTrace();
    }

}
    @Override
    public void run() {
        while (isRunning) {
            DatagramPacket datagramPacket = new DatagramPacket(new byte[2048], 2048);
            try {
                multicastSocket.receive(datagramPacket);
                String s = new String(datagramPacket.getData(), 0, datagramPacket.getLength()); //la stringa contiene: nomeProgetto utente messaggio
                String[] strings = s.split("\n");
                String projectName = strings[0];
                Message message = new Message(strings[1],strings[2]);
                synchronized (messaggesQueue) {
                    messaggesQueue.get(projectName).add(message);
                }

            } catch (IOException e) {
                isRunning = false;
            }

        }
    }

    public void closeChats(){
        synchronized (infoProjects) {
            for (InetAddress ipChats : infoProjects.values()) {
                try {
                    multicastSocket.leaveGroup(ipChats);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            infoProjects.clear();
            messaggesQueue.clear();

        }


    }

    public void joinChat(String projectName,String ipMulticast) {
        try {
            InetAddress inetAddress = InetAddress.getByName(ipMulticast);
            synchronized (infoProjects) {
                if (!infoProjects.containsKey(projectName))
                    infoProjects.put(projectName, inetAddress);
            }
            synchronized (messaggesQueue){
                if(!messaggesQueue.containsKey(projectName))
                    messaggesQueue.put(projectName,new ArrayList<>());
            }
            multicastSocket.joinGroup(inetAddress);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void leaveChat(String projectName) {
        synchronized (infoProjects) {
            if (infoProjects.containsKey(projectName)) {
                InetAddress address = infoProjects.get(projectName);
                infoProjects.remove(projectName);
                try {
                    multicastSocket.leaveGroup(address);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }
    public void readChat(String projectName) {
        synchronized (messaggesQueue) {
            ArrayList<Message> listMessagges = messaggesQueue.get(projectName); //accedo alla lista di messaggi di quel progetto
            if(listMessagges == null)
                System.out.println("Non partecipi alla chat del progetto "+projectName);
            else {
                for (Message m : listMessagges) {
                    System.out.println("< Messaggio da " + m.getSender() + ": " + m.getMessage());
                }
                System.out.println("< Non ci sono più messaggi da leggere in questa chat");
                messaggesQueue.get(projectName).clear(); //cancello i messaggi letti
            }

        }

    }

    public void sendMessage(String projectName, String message,String userLoggedIn){
        synchronized (infoProjects){
            InetAddress ip = infoProjects.get(projectName);
            if(ip == null)
                System.out.println("Non partecipi alla chat " + projectName);
            else{
                String messTosend = projectName + "\n" + userLoggedIn + "\n" + "' " + message + "'";
                byte[] array = messTosend.getBytes(StandardCharsets.UTF_8);
                DatagramPacket datagramPacket = new DatagramPacket(array,array.length,ip,multiPort);
                try {
                    multicastSocket.send(datagramPacket);
                    System.out.println("Messaggio inviato");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void closeSocket(){
        multicastSocket.close();
    }

}
