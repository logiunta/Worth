package Client;

import Common.*;

import Exceptions.UserAlreadySignedInException;

import java.io.*;
import java.net.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;


public class ClientMain {
    private static final int rmiPort = 54500;
    private static final int rmi_callbackPort = 53780;
    private static final int tcpPort = 51150;

    private static RmiClientNotifyInterface callbackObject;
    private static RmiServerNotifyInterface serverCallback;
    private static RmiClientNotifyInterface stub;
    private static LocalDb localUsersDb;
    public static BufferedReader inputStream;
    public static BufferedWriter outputStream;
    private static String op_message;
    private static boolean isLoggedIn; // true, se si è loggati tramite questo client
    private static MulticastConnection myChats;
    private static String userLoggedIn;

    public static void main(String[] args) {
        localUsersDb = new LocalDb();
        isLoggedIn = false;
        myChats = new MulticastConnection();
        userLoggedIn = null;
        String op;
        String[] my_args;
        boolean closeClient = false;

        try(Socket socket = new Socket();)
        {
            Registry registerReg = LocateRegistry.getRegistry(rmiPort);
            RmiServerInterfaceRegistration registrationObject = (RmiServerInterfaceRegistration) registerReg.lookup("SignUpService");

            Registry notifyReg = LocateRegistry.getRegistry(rmi_callbackPort);
            serverCallback = (RmiServerNotifyInterface) notifyReg.lookup("NotifyService");

            callbackObject = new RmiClientNotifyImpl(localUsersDb,myChats);
            stub = (RmiClientNotifyInterface) UnicastRemoteObject.exportObject(callbackObject,0);

            socket.connect(new InetSocketAddress(InetAddress.getLocalHost(),tcpPort));
            BufferedReader buffered_cmd = new BufferedReader(new InputStreamReader(System.in));

            System.out.println("Benvenuto in WORTH");
            System.out.println("Prima di iniziare effettua il login se sei registrato, altrimenti registrati");
            System.out.println("Digita help se hai bisogno di supporto!");


            while(!closeClient) {
                System.out.print("> ");
                op_message = buffered_cmd.readLine();
                my_args = op_message.split(" "); //splitta il messaggio dove ci sono gli spazi
                op = my_args[0];
                inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                switch (op) {
                    case "help":
                        showHelpCommand(my_args);
                        break;

                    case "login":
                        login(my_args);
                        break;

                    case "register":
                        registration(my_args, registrationObject);
                        break;

                    case "logout":
                        logout(my_args);
                        clearLocalDb();
                        break;

                    case "listUsers":
                        listUsers(my_args);
                        break;

                    case "listOnlineUsers":
                        listOnlineUsers(my_args);
                        break;

                    case "addMember":
                        addMember(my_args);
                        break;

                    case "showMembers":
                        showMembers(my_args);
                        break;

                    case "readChat":
                        readChat(my_args);
                        break;

                    case "sendChatMsg":
                        sendChatMsg(my_args);
                        break;

                    case "close":
                        close(my_args);
                        if(isLoggedIn)
                            break;
                        else {
                            closeClient = true;
                            break;
                        }

                    case "showCard":
                        showCard(my_args);
                        break;

                    case "createProject":
                        createProject(my_args);
                        break;

                    case "addCard":
                        addCard(my_args);
                        break;

                    case "showCards":
                        showCards(my_args);
                        break;

                    case "moveCard":
                        moveCard(my_args);
                        break;

                    case "listProjects":
                        listProjects(my_args);
                        break;

                    case "cancelProject":
                        cancelProject(my_args);
                        break;

                    case "getCardHistory":
                        getCardHistory(my_args);
                        break;

                    case "":
                        break;

                    default:
                        System.out.println(checkArgs(my_args,"error"));
                        break;

                }
            }


        } catch (NotBoundException | IOException e){
            try {
                UnicastRemoteObject.unexportObject(callbackObject, false);
                outputStream.close();
                inputStream.close();
                myChats.closeSocket();
                System.out.println("Impossibile comunicare col server: riprovare più tardi");

            } catch (IOException e2) {
                System.out.println("Impossibile comunicare col server: riprovare più tardi");
            }
        }
    }


    private static void showHelpCommand(String[] myargs) {
        String res = checkArgs(myargs,"help");
        if(res.equals("ok")) {
            System.out.println("------------------------Comandi disponibili in Worth------------------------");
            System.out.println("Attenzione: la sintassi va rispettata e i comandi vanno scritti in minuscolo");
            res = "Registrazione a Worth: register nickName password" + "\nAccedere a Worth: login nickName password" +
                    "\nLista degli utenti registrati a Worth: listUsers" +
                    "\nLista degli utenti online: listOnlineUsers" + "\nUscire da Worth: logout nickName" +
                    "\nLista dei progetti a cui partecipo: listProjects" + "\nCreare un progetto: createProject projectName" +
                    "\nAggiungere un membro ad un progetto: addMember projectName nickName" +
                    "\nMostrare i membri di un progetto: showMembers projectName" + "\nMostrare le info di una card: showCard projectName cardName" +
                    "\nMostrare le card di un progetto: showCards projectName" +
                    "\nAggiungere card ad un progetto: addCard projectName cardName description" +
                    "\nSpostare una card: moveCard projectName cardName fromList toList" +
                    "\nMostrare lo storico degli spostamenti di una card: getCardHistory projectName cardName" +
                    "\nLeggere la chat: readChat projectName" + "\nScrivere in chat: sendChatMsg projectName mex" +
                    "\nCancellare un progetto: cancelProject projectName" + "\nChiudere il client: close";
        }
        System.out.println(res);

    }



    private static String checkArgs(String[] myargs,String op){
        String res = "ok";
        switch (op) {
            //sintassi per OR
            case "register":
            case "login":
                if (myargs.length < 3)
                    res = "Comando errato, argomenti mancanti\nUso: " + op + " nickname password";
                else if (myargs.length > 3)
                    res = "Comando errato, troppi argomenti\nUso: " + op + " nickname password";
                break;
            case "logout":
                if (myargs.length < 2)
                    res = "Comando errato, argomenti mancanti\nUso: " + op + " nickname";
                else if (myargs.length > 2)
                    res = "Comando errato, troppi argomenti\nUso: " + op + " nickname";
                break;

            case "createProject":
            case "showMembers":
            case "cancelProject":
            case "showCards":
            case "readChat":
                if (myargs.length < 2)
                    res = "Comando errato, argomenti mancanti\nUso: " + op + " projectName";
                else if (myargs.length > 2)
                    res = "Comando errato, troppi argomenti\nUso: " + op + " projectName";
                break;

            case "addMember":
                if(myargs.length < 3)
                    res = "Comando errato, argomenti mancanti\nUso: " + op + " projectName nickName";
                else if(myargs.length > 3)
                    res = "Comando errato, troppi argomenti\nUso: " + op + " projectName nickName";
                break;
            case "moveCard":
                if(myargs.length < 5)
                    res = "Comando errato, argomenti mancanti\nUso: " + op + " projectName cardName listaPartenza listaDestinazione";
                else if(myargs.length > 5)
                    res = "Comando errato, troppi argomenti\nUso: " + op + " projectName cardName listaPartenza listaDestinazione";
                break;
            case "help":
            case "listUsers":
            case "listOnlineUsers":
            case "close":
            case "listProjects":
                if(myargs.length > 1)
                    res = "Comando errato, troppi argomenti\nUso: " + op;
                else if (myargs.length < 1)
                    res = "Comando errato, argomenti mancanti\nUso: " + op;
                break;

            case "addCard":
                if(myargs.length > 4)
                    res = "Comando errato, troppi argomenti\nUso: " + op + " projectName cardName description";
                else if(myargs.length < 4)
                    res = "Comando errato, argomenti mancanti\nUso: " + op + " projectName cardName description\n" +
                            "Ricorda: la descrizione deve essere breve e priva di spazi";
                break;

            case "sendChatMsg":
                if(myargs.length < 3)
                    res = "Comando errato, argomenti mancanti\nUso: " + op + " projectName message";
                else if(myargs.length > 3)
                    res = "Comando errato, troppi argomenti\nUso: " + op + " projectName message";
                break;

            case "showCard":
            case "getCardHistory":
                if(myargs.length < 3)
                    res = "Comando errato, argomenti mancanti\nUso: " + op + " projectName cardName";
                else if(myargs.length > 3)
                    res = "Comando errato, troppi argomenti\nUso: " + op + " projectName cardName";
                break;


            case "error":
                res = "Comando non trovato: prova ad usare lettere minuscole e '_' al posto degli spazi o digita help " +
                        "per conoscere quali comandi sono disponibili";
                break;

        }

        return res;
    }

    private static void showMembers(String[] my_args) throws IOException {
        String res = checkArgs(my_args, "showMembers");
        if (res.equals("ok")) {
            if (!isLoggedIn) {
                res = "Errore: login non effettuato";
                System.out.println(res);
            } else {
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                System.out.print("< ");
                while (!(res = inputStream.readLine()).equals("finished")) {
                    System.out.println(res);

                }
            }
        } else
            System.out.println(res);

    }

    private static void getCardHistory(String[] my_args) throws IOException {
        String res = checkArgs(my_args, "getCardHistory");
        if (res.equals("ok")) {
            if (!isLoggedIn) {
                res = "Errore: login non effettuato";
            }
            else {
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                res = inputStream.readLine();
            }
        }
        System.out.println("< " + res);
    }

    private static void showCard(String[] my_args) throws IOException {
        String res = checkArgs(my_args, "showCard");
        if (res.equals("ok")) {
            if (!isLoggedIn) {
                res = "Errore: login non effettuato";
                System.out.println(res);

            }
            outputStream.write(op_message + "\r\n");
            outputStream.flush();
            System.out.print("< ");
            while (!(res = inputStream.readLine()).equals("finished")) {
                System.out.println(res);

            }
        } else
            System.out.println(res);
    }

    private static void sendChatMsg(String[] my_args) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int i = 2; i < my_args.length; i++) {
            builder.append(my_args[i] + " ");
        }
        String message = builder.toString();
        String[] args = new String[3];
        args[0] = my_args[0]; //command
        args[1] = my_args[1]; //nome progetto
        args[2] = message;

        String res = checkArgs(args, "sendChatMsg");
        if (res.equals("ok")) {
            if (!isLoggedIn)
                res = "Errore: login non effettuato";

            else {
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                res = inputStream.readLine();
                if (res.equals("ok")) { // il server da il via libera
                    myChats.sendMessage(my_args[1], message,userLoggedIn);
                } else
                    System.out.println("< " + res);
            }

        }
        else
            System.out.println("< "+res);
    }
    private static void readChat(String[] my_args) throws IOException{
        String res = checkArgs(my_args,"readChat");
        if(res.equals("ok")) {
            if (!isLoggedIn) {
                res = "Errore: login non effettuato";
                System.out.println(res);

            } else {
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                res = inputStream.readLine();
                if(res.equals("ok")){ // il server da il via libera
                    myChats.readChat(my_args[1]);

                }
                else
                    System.out.println("< "+res);
            }
        }
        else
            System.out.println("< "+res);
    }

    private static void showCards(String[] my_args) throws IOException{
        String res = checkArgs(my_args,"showCards");
        if (res.equals("ok")) {
            if (!isLoggedIn) {
                res = "Errore: login non effettuato";
                System.out.println(res);
            } else {
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                System.out.print("< ");
                while (!(res = inputStream.readLine()).equals("finished")) {
                    System.out.println(res);

                }
            }
        } else
            System.out.println(res);

    }

    private static void cancelProject(String[] my_args) throws IOException {
        String res = checkArgs(my_args,"cancelProject");
        if(res.equals("ok")) {
            if (!isLoggedIn) {
                res = "Errore: login non effettuato";
            }
            else{
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                res = inputStream.readLine();
            }
        }
        System.out.println("< "+res);
    }


    private static void addMember(String[] my_args) throws IOException {
        String res = checkArgs(my_args, "addMember");
        if(res.equals("ok")){
            if(!isLoggedIn)
                res = "Errore: login non effettuato";

            else {
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                res = inputStream.readLine();
            }
        }
        System.out.println("< "+res);
    }

    private static void moveCard(String[] my_args) throws IOException{
        String res = checkArgs(my_args,"moveCard");
        if(res.equals("ok")) {
            if (!isLoggedIn)
                res = "Errore: login non effettuato";
            else {
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                res = inputStream.readLine();
            }

        }
        System.out.println("< " + res);
    }
    private static void listProjects(String[] my_args) throws IOException {
        String res = checkArgs(my_args,"listProjects");

        if (res.equals("ok")) {
            if (!isLoggedIn) {
                res = "Errore: login non effettuato";
                System.out.println(res);
            }

            else {
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                System.out.print("< ");
                while(!(res = inputStream.readLine()).equals("finished")){
                    System.out.println(res);

                }
            }
        }
        else {
            System.out.println(res);
        }

    }

    private static void addCard(String[] my_args) throws IOException {
        String res = checkArgs(my_args, "addCard");
        if (res.equals("ok")) {
            if (!isLoggedIn)
                res = "Errore: login non effettuato";
            else {
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                res = inputStream.readLine();
            }

        }
        System.out.println("< "+res);
    }


    private static void createProject(String[] my_args) throws IOException {
        String res = checkArgs(my_args,"createProject");
        String mess = null;
        String response = "";
        if(res.equals("ok")){
            if(!isLoggedIn){
                res = "Errore: login non effettuato";
            }
            else{
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                while (!(mess = inputStream.readLine()).equals("finished")) {
                    response = response.concat(mess + "\n");
                }

                res = response.split("\n")[0];
                if(res.equals("Progetto creato correttamente")){
                    myChats.joinChat(my_args[1],response.split("\n")[1]); //(projectName,ipMulticast)

                }

            }
        }

        System.out.println("< "+res);
    }

    private static void close(String[] myargs) throws IOException {
        String res = checkArgs(myargs,"close");
        if(res.equals("ok")) {
            if (isLoggedIn) {
                res = "Attenzione, prima di chiudere il client effettuare il logout";

            } else {
                UnicastRemoteObject.unexportObject(callbackObject, false);
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                outputStream.close();
                inputStream.close();
                myChats.closeSocket();
                userLoggedIn = null;
                return;
            }
        }

        System.out.println("< "+res);
    }

    private static void registration(String[] myargs,RmiServerInterfaceRegistration registrationObject) throws RemoteException {
        String res = checkArgs(myargs,"register");

        if (res.equals("ok")){
            if(isLoggedIn)
                res = "Login già effettuato, disconnettersi prima di poter effettuare una nuova registrazione";
            else {
                try {
                    res = registrationObject.signUp(myargs);
                }
                catch (UserAlreadySignedInException e){
                    res = ("Utente " + myargs[1] + " già registrato a Worth");
                }

            }

        }

        System.out.println("< "+res);

    }

    private static void login(String[] myargs) throws IOException {
        String res = checkArgs(myargs,"login");
        String mess = null;
        String response = "";

        if(res.equals("ok")) {
            if (isLoggedIn) {
                res = "Attenzione: login già effettuato, effettuare prima il logout";

            } else {
                serverCallback.signUpForCallBack(stub, myargs[1]);
                outputStream.write(op_message + "\r\n"); // invia la richiesta al server
                outputStream.flush();
                while (!(mess = inputStream.readLine()).equals("finished")) {
                    response = response.concat(mess + "\n");
                }

                res = response.split("\n")[0];

                if (!res.equals("login effettuato correttamente")) {
                    serverCallback.signOutFromCallBack(stub);
                } else {
                    isLoggedIn = true;
                    userLoggedIn = stub.getIdClient(); //dallo stub ottengo il nome utente associato al client
                    String[] connectionsInfo = response.split("\n");
                    //array di stringhe dove ogni stringa è: nomeProgetto ipProgetto
                    if (connectionsInfo.length > 1) {
                        for (int i = 1; i < connectionsInfo.length; i++) {
                            String projectName = connectionsInfo[i].split(" ")[0];
                            String ipMulticast = connectionsInfo[i].split(" ")[1];
                            myChats.joinChat(projectName, ipMulticast);
                        }
                    }

                    new Thread(myChats).start();//thread che gestisce le chat a cui partecipa l'utente

                }
            }

        }

        System.out.println("< "+res);


    }

    private static void logout(String[] myargs) throws IOException {
        String res = checkArgs(myargs,"logout");

        if(res.equals("ok")) {
            if(!isLoggedIn){
                res = "Errore: login non effettuato";
            }
            else {
                outputStream.write(op_message + "\r\n");
                outputStream.flush();
                res = inputStream.readLine();
                if (res.equals("Logout effettuato correttamente")) {
                    serverCallback.signOutFromCallBack(stub);
                    isLoggedIn = false;
                    userLoggedIn = null;
                    myChats.closeChats();


                }
            }

        }

        System.out.println("< " + res);

    }

    private static void clearLocalDb(){
        localUsersDb.clear();
    }


    private static void listUsers(String[] my_args) {
        String res = checkArgs(my_args,"listUsers");
        if(!isLoggedIn)
            res = "Errore: effettuare prima il login";

        if(res.equals("ok")) {
            ArrayList<String> list = localUsersDb.getListUsers();
            if (list.size() == 0)
                res = "Nessun utente trovato nel Db locale";

            else {
                for (String user : list) {
                    System.out.println(user);
                }
            }
        }
        else System.out.println(res);

    }

    private static void listOnlineUsers(String[] my_args) {
        String res = checkArgs(my_args,"listOnlineUsers");
        if(!isLoggedIn)
            res = "Errore: effettuare prima il login";

        if(res.equals("ok")) {
            ArrayList<String> list = localUsersDb.getListOnlineUsers();
            if (list.size() == 0)
                res = "Tutti gli utenti sono offline";
            else {
                for (String user : list) {
                    System.out.println(user);
                }
            }
        }

        else System.out.println(res);

    }

}
