package Server;

import Common.*;
import Exceptions.*;


import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.util.ArrayList;

public class OperationHandler implements Runnable {
    private final int multiPort = 63000;
    private final int udpPort = 54150;
    private final UsersDatabase usersDb; //importante usare final quando dovrò gestire la concorrenza su questo oggetto
    private final ProjectsDatabase projectsDb;
    private final Socket socket;
    private boolean loggedIn;
    private final RmiServerNotifyImpl notify;
    private String userLoggedIn; //usato per indicare quale utente è loggato con questo client
    private final MultiGenerator multiGenerator;

    public OperationHandler(UsersDatabase usersDb, Socket socket, RmiServerNotifyImpl notify, MultiGenerator multiGenerator, ProjectsDatabase projectsDb) {
        this.usersDb = usersDb;
        this.projectsDb = projectsDb;
        this.socket = socket;
        loggedIn = false;
        this.notify = notify;
        userLoggedIn = null;
        this.multiGenerator = multiGenerator;

    }

    @Override
    public void run() {
        executeOp();

    }

    private void executeOp() {
        String mess;
        String[] myArgs;
        String res = null;
        String op;

        try (BufferedWriter outputStream = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader inputStream = new BufferedReader((new InputStreamReader(socket.getInputStream())));)
        {
            loop:
            while ((mess = inputStream.readLine()) != null) {
                myArgs = mess.split(" ");
                op = myArgs[0];

                switch (op) {
                    case "close":
                        socket.close();
                        System.out.println("Un client si è disconnesso");
                        break loop; //esce dal loop

                    case "login":
                        res = loginHandler(myArgs[1], myArgs[2]);
                        outputStream.write(res.concat("finished" + "\r\n"));
                        outputStream.flush();
                        break;

                    case "logout":
                        res = logoutHandler(myArgs[1]);
                        outputStream.write(res + "\r\n");
                        outputStream.flush();
                        break;

                    case "createProject":
                        res = createProjectHandler(myArgs[1]);
                        outputStream.write(res.concat("finished" + "\r\n"));
                        outputStream.flush();
                        break;

                    case "cancelProject":
                        res = deleteProjectHandler(myArgs[1]);
                        outputStream.write(res + "\r\n");
                        outputStream.flush();
                        break;
                    case "showMembers":
                        res = showMembersHandler(myArgs[1]);
                        outputStream.write(res.concat("finished" + "\r\n"));
                        outputStream.flush();
                        break;

                    case "showCard":
                        res = showCardHandler(myArgs[1],myArgs[2]);
                        outputStream.write(res.concat("finished" + "\r\n"));
                        outputStream.flush();
                        break;

                    case "addCard":
                        res = addCardHandler(myArgs);
                        outputStream.write(res + "\r\n");
                        outputStream.flush();
                        break;

                    case "moveCard":
                        res = moveCardHandler(myArgs[1], myArgs[2], myArgs[3], myArgs[4]);
                        outputStream.write(res + "\r\n");
                        outputStream.flush();
                        break;
                    case "showCards":
                        res = showCardsHandler(myArgs[1]);
                        outputStream.write(res.concat("finished" + "\r\n"));
                        outputStream.flush();
                        break;

                    case "listProjects":
                        res = listProjectsHandler();
                        outputStream.write(res.concat("finished" + "\r\n"));
                        outputStream.flush();
                        break;

                    case "addMember":
                        res = addMemberHandler(myArgs[1], myArgs[2]);
                        outputStream.write(res + "\r\n");
                        outputStream.flush();
                        break;

                    case "sendChatMsg":
                        res = chatHandler(myArgs[1]);
                        outputStream.write(res + "\r\n");
                        outputStream.flush();
                        break;

                    case "readChat":
                        res = chatHandler(myArgs[1]);
                        outputStream.write(res + "\r\n");
                        outputStream.flush();
                        break;

                    case "getCardHistory":
                        res = getCardHistoryHandler(myArgs[1],myArgs[2]);
                        outputStream.write(res + "\r\n");
                        outputStream.flush();
                        break;

                }
            }
        } catch (SocketException e1) {
            System.out.println("La connessione al client " + socket.getInetAddress().getHostName() + " si è arrestata in modo anomalo");
            interruptHandler();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private String getCardHistoryHandler(String projectName, String cardName) {
        String res = cardName + " history: [ ";
        ArrayList<String> list;
        if (!loggedIn)
            res = "Errore: login non effettuato";
        else {
            try {
                list = projectsDb.getProjectHistory(projectName, cardName, userLoggedIn);
                int lenght = list.size();
                for (int i = 0; i < lenght - 1; i++){
                    res = res.concat(list.get(i) + ", ");
                }
                res = res.concat(list.get(lenght-1) + " ]");

            } catch (ProjectNotFoundException e) {
                res = "Errore: il progetto " + projectName + " non esiste";
            } catch (CardNotFoundException e) {
                res = "Errore: la card " + cardName + " non è stata trovata nel progetto " + projectName;
            } catch (NotPermittedException e) {
                res = "Errore: non hai i permessi per fare questa operazione";
            }
        }
        return res;
    }


    private String showCardHandler(String projectName, String cardName){
        String res = "Info sulla card " + cardName + "\n";
        String info;
        if (!loggedIn)
            res = "Errore: login non effettuato\n";
        else{
            try {
                info = projectsDb.getCardInfo(projectName,cardName,userLoggedIn);
                res = res.concat(info + "\n");

            } catch (ProjectNotFoundException e) {
                res = "Errore: il progetto " + projectName + " non esiste\n";
            } catch (CardNotFoundException e) {
               res = "Errore: la card " + cardName + " non è stata trovata nel progetto " + projectName +"\n";
            } catch (NotPermittedException e) {
                res = "Errore: non hai i permessi per fare questa operazione\n";
            }
        }
        return res;
    }

    private String showCardsHandler(String projectName){
        String res = "Le cards del progetto " + projectName + ":\n";
        ArrayList<String> list;
        if (!loggedIn)
            res = "Errore: login non effettuato\n";
        else {
            try {
                list = projectsDb.getCardsProject(projectName, userLoggedIn);
                for(String s : list)
                    res = res.concat(s + "\n");

            } catch (ProjectNotFoundException e) {
                res = "Errore: il progetto " + projectName + " non esiste\n";
            } catch (NotPermittedException e) {
                res = "Errore: non hai i permessi per fare questa operazione\n";
            } catch (NoCardsExceptions e) {
                res = "Non ci sono cards nel progetto " + projectName + "\n";
            }

        }
        return res;

    }

    private String moveCardHandler(String projectName, String cardName, String listaPartenza, String listaDestinazione) {
        String res = "Card spostata correttamente";
        if (!loggedIn)
            res = "Errore: login non effettuato";

        else if (listaPartenza.equals(listaDestinazione))
            res = "Errore: le liste coincidono";

        else {
            String user = userLoggedIn; //l'utente che fa la richiesta
            try {
                projectsDb.moveCard(projectName, cardName, listaPartenza, listaDestinazione, user);
                String messagge = messageForMovedCard(projectName,"Worth",userLoggedIn,cardName,listaPartenza,listaDestinazione);
                String ipChat = getMultiInfo(projectName).split(" ")[1];
                byte[] array = messagge.getBytes(StandardCharsets.UTF_8);
                DatagramSocket datagramSocket = new DatagramSocket(udpPort);
                DatagramPacket datagramPacket = new DatagramPacket(array, array.length, InetAddress.getByName(ipChat), multiPort);
                datagramSocket.send(datagramPacket);
                datagramSocket.close();

            } catch (ProjectNotFoundException e) {
                res = "Errore: il progetto " + projectName + " non esiste";
            } catch (NotPermittedException e) {
                res = "Errore: non hai i permessi per fare questa operazione";
            } catch (IllegalArgumentException e) {
                res = "Errore: le liste non sono corrette. Liste disponibili (todo - inprogress - toberevised - done)";
            } catch (ListMisMatchException e) {
                res = "Errore: la transizione " + listaPartenza + " -> " + listaDestinazione + " non è permessa";
            } catch (CardNotFoundException e) {
                res = "Errore: la card " + cardName + " non è presente nella lista "+ listaPartenza + " del progetto " +projectName;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return res;


    }

    private String chatHandler(String projectName) {
        String res = "ok";
        if (!loggedIn)
            res = "Errore: login non effettuato";

        else {
            try {
                if (projectsDb.userInProject(projectName, userLoggedIn)) //se l'utente che fa richiesta ha i permessi
                    res = "ok";
            } catch (ProjectNotFoundException e) {
                res = "Errore: il progetto non esiste";
            }
        }

        return res;

    }

    private String showMembersHandler(String projectName){
        String res = "I membri del progetto " + projectName + ":\n";
        ArrayList<String> list;
        if (!loggedIn) {
            res = "Errore: login non effettuato\n";

        } else {
            try {
                list = projectsDb.getMembersProject(projectName, userLoggedIn);
                for(String s : list)
                    res = res.concat(s + "\n");

            } catch (ProjectNotFoundException e) {
                res = "Errore: il progetto " + projectName + " non esiste\n";
            } catch (NotPermittedException e) {
                res = "Errore: non hai i permessi per fare questa operazione\n";
            } catch (NullPointerException e){
                e.printStackTrace();

            }

        }
        return res;

    }

    private String deleteProjectHandler(String projectName) {
        String res = "Progetto eliminato correttamente";
        if (!loggedIn)
            res = "Errore: login non effettuato";
        else {
            try {
                Project project = projectsDb.getProject(projectName);
                String ipProject = projectsDb.getProjectIp(projectName);
                projectsDb.removeProject(projectName, userLoggedIn, multiGenerator, ipProject); //effettua i controlli su eventuali valori null di project e iproject
                notify.doProjectDeletedCallback(project, userLoggedIn);

            } catch (ProjectNotFoundException e) {
                res = "Errore: il progetto " + projectName + " non esiste";
            } catch (NotPermittedException e) {
                res = "Errore: non hai i permessi per fare questa operazione";
            } catch (NotAllDoneException e) {
                res = "Errore: non è ancora possibile eliminare il progetto. Tutte le cards devono essere nello stato 'done'";
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
        return res;
    }

    private String listProjectsHandler() throws IOException {
        String res = "Progetti a cui partecipi:\n";
        ArrayList<String> usersProject;
        if (!loggedIn) {
            res = "Errore: login non effettuato\n";

        } else {
            String user = userLoggedIn; //l'utente che fa la richiesta
            try {
                usersProject = projectsDb.getListOfProjects(user);

                for (String s : usersProject) {
                    res = res.concat(s + "\n");
                }

            } catch (NullPointerException e) {
                res = "Errore: campo nullo\n";


            } catch (NoProjectsException e) {
                res = "Non ci sono progetti a cui partecipi\n";

            }

        }
        return res;

    }

    private String addMemberHandler(String projectName, String nickName) {
        String res = "Utente " + nickName + " aggiunto al progetto " + projectName;
        if (!loggedIn)
            res = "Errore: login non effettuato";
        else {
            try {
                if (usersDb.isInWorth(nickName)) {
                    Project project = projectsDb.addMember(projectName, nickName, userLoggedIn);
                    notify.doMulticastCallback(nickName, project);
                }

            } catch (ProjectNotFoundException e) {
                res = "Errore: il progetto " + projectName + " non esiste";

            } catch (NotPermittedException e) {
                res = "Errore: non hai i permessi per questo progetto";

            } catch (UserAlreadyAddedException e) {
                res = "Errore: l'utente " + nickName + " fa già parte di questo progetto";

            } catch (UserNotFoundException e) {
                res = "Errore: l'utente " + nickName + " non è iscritto a Worth";

            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
        return res;
    }

    private String[] parseCardDescription(String[] my_args){
        StringBuilder builder = new StringBuilder();
        for (int i = 3; i < my_args.length; i++) {
            builder.append(my_args[i] + " ");
        }
        String description = builder.toString();
        String[] args = new String[4];
        args[0] = my_args[0]; //command
        args[1] = my_args[1]; //nome progetto
        args[2] = my_args[2]; //nome card
        args[3] = description;

        return args;
    }

    private String addCardHandler(String[] my_args) {
        String res = "Card aggiunta correttamente";
        String[] args = parseCardDescription(my_args);
        String projectName = args[1];
        String cardName = args[2];
        String description = args[3];

        if (!loggedIn)
            res = "Errore: login non effettuato";
        else {
            String user = userLoggedIn; //l'utente che fa la richiesta
            try {
                projectsDb.addCard(projectName, cardName, description, user);
                String message = messageForAddedCard(projectName,"Worth",userLoggedIn,cardName);
                String ipChat = getMultiInfo(projectName).split(" ")[1];
                byte[] array = message.getBytes(StandardCharsets.UTF_8);
                DatagramSocket datagramSocket = new DatagramSocket(udpPort);
                DatagramPacket datagramPacket = new DatagramPacket(array, array.length, InetAddress.getByName(ipChat), multiPort);
                datagramSocket.send(datagramPacket);
                datagramSocket.close();

            } catch (CardAlreadyExistException e) {
                res = "Errore: la card " + cardName + " è già presente nel progetto " + projectName;
            } catch (ProjectNotFoundException e) {
                res = "Errore: il progetto " + projectName + " non esiste";
            } catch (NotPermittedException e) {
                res = "Errore: non puoi hai i permessi per questo progetto";

            } catch (NullPointerException | IOException e) {
                e.printStackTrace();
            }

        }
        return res;

    }

    private String createProjectHandler(String projectName) {
        String res = "Progetto creato correttamente\n";
        if (!loggedIn)
            res = "Errore: login non effettuato\n";
        else {
            String user = userLoggedIn;
            try {
                String ip = projectsDb.addProject(projectName, user, multiGenerator);
                res = res.concat(ip + "\r\n");

            } catch (ProjectAlreadyAddedException e) {
                res = "Il progetto esiste già, impossibile completare l'operazione\n";
            } catch (NullPointerException e) {
                res = "Errore: alcuni campi nulli\n";
            } catch (IllegalArgumentException e) {
                res = "Errore porta negativa\n";
            }
        }


        return res;
    }

    private void interruptHandler() {
        ArrayList<UserStatus> clientsDb = null;
        if (loggedIn) {
            String user = userLoggedIn;
            try {
                clientsDb = usersDb.logoutDb(user, userLoggedIn);

            } catch (UserNotFoundException e) {
                System.out.println("Errore: Utente non iscritto a Worth!");
            } catch (NotPermittedException e) {
                System.out.println("Errore: non hai i permessi per disconnettere un'altro utente");
            }
            try {
                notify.update(clientsDb, "interrupted", user);
                loggedIn = false;
                userLoggedIn = null;
            } catch (RemoteException e) {
                e.printStackTrace();
            }

        }
    }

    private String loginHandler(String nickName, String passw) {
        String res = "login effettuato correttamente\n";
        ArrayList<UserStatus> clientsDb;
        ArrayList<String> userProjects;
        ArrayList<String> infoMultiConnection = new ArrayList<>();

        if (loggedIn) {
            res = "Attenzione: login già effettuato, effettuare prima il logout\n";

        } else {
            try {
                clientsDb = usersDb.loginDb(nickName, passw);
                userLoggedIn = nickName;
                loggedIn = true;
                try {
                    userProjects = projectsDb.getListOfProjects(userLoggedIn);
                    for (String project : userProjects) {
                        infoMultiConnection.add(getMultiInfo(project));
                    }

                    for (String ims : infoMultiConnection) {
                        res = res.concat(ims + "\r\n");
                    }
                    notify.update(clientsDb, "statusOn", nickName); //aggiorna il db di tutti i client connessi

                } catch (NoProjectsException e) {
                    notify.update(clientsDb, "statusOn", nickName); //aggiorna il db di tutti i client connessi

                }

            } catch (RemoteException e) {
                e.printStackTrace();
            } catch (UserNotFoundException e) {
                res = ("Errore: Utente non iscritto a Worth!\n");
            } catch (PasswException e) {
                res = ("Errore: Password sbagliata\n");
            } catch (UserAlreadyConnectedException e) {
                res = ("Utente " + nickName + " già connesso\n");
            }

        }

        return res;

    }


    private String getMultiInfo(String projectName){
        String ip = projectsDb.getProjectIp(projectName);
        return projectName + " " + ip;

    }


    private String logoutHandler(String nickName) {
        String res = "Logout effettuato correttamente";
        ArrayList<UserStatus> clientsDb = null;
        if (!loggedIn)
            res = "Errore: login non effettuato";
        else {
            try {
                clientsDb = usersDb.logoutDb(nickName, userLoggedIn);
                loggedIn = false;
                userLoggedIn = null;
                try {
                    notify.update(clientsDb, "statusOff", nickName);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }

            } catch (UserNotFoundException e) {
                res = ("Errore: Utente non iscritto a Worth!");
            } catch (NotPermittedException e) {
                res = ("Errore: non hai i permessi per disconnettere un'altro utente");
            }

        }
        return res;
    }

    private String messageForMovedCard(String projectName, String sender, String userLoggedIn, String cardName, String lista1, String lista2) {
        String mess;
        mess = userLoggedIn + " ha spostato la card '" + cardName + "' dallo stato '" + lista1 + "' allo stato '" + lista2 + "'";
        return projectName + "\n" + sender + "\n" + mess;

    }

    private String messageForAddedCard(String projectName, String sender, String userLoggedIn, String cardName) {
        String mess;
        mess = userLoggedIn + " ha aggiunto la card '" + cardName + "' a questo progetto";
        return projectName + "\n" + sender + "\n" + mess;


    }


}



