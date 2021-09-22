package Server;

import Common.User;
import Common.UserStatus;
import Exceptions.*;
import Server.Storage;

import java.io.*;
import java.util.*;

public class UsersDatabase implements Serializable{
    private final HashMap<String, User> users;

    public UsersDatabase() {
        users = Storage.restoreUsers();

    }


    public ArrayList<UserStatus> addUser(String nickName, String passw) throws NullPointerException, UserAlreadySignedInException {
        if (nickName == null || passw == null)
            throw new NullPointerException();

        else {
            User newUser = new User(nickName, passw);
            synchronized (users) {
                if (users.putIfAbsent(nickName, newUser) != null) {
                    throw new UserAlreadySignedInException();
                }
                ArrayList<User> listOfUsers = new ArrayList<>(users.values());
                Storage.writeUsersToJson(listOfUsers);
            }
        }
        return getDbForClients();

    }

        private void setUserStatus(String nickname) {
            synchronized (users) {
                users.get(nickname).setStatus("online");
                ArrayList<User> listOfUsers = new ArrayList<>(users.values());
                Storage.writeUsersToJson(listOfUsers);


            }

        }

    public ArrayList<UserStatus> loginDb(String nickName, String passw) throws NullPointerException, UserNotFoundException, PasswException, UserAlreadyConnectedException {
        if(nickName == null || passw == null)
            throw new NullPointerException();
        synchronized (users) {
            User u = users.get(nickName);
            if (u == null)
                throw new UserNotFoundException();
            if (!(u.getPassw().equals(passw)))
                throw new PasswException();

            if(u.getStatus().equals("online"))
                throw new UserAlreadyConnectedException();

            setUserStatus(nickName);
        }

        return getDbForClients(); //ritorno la struttura dati aggiornata da passare ai client

    }

    public ArrayList<UserStatus> logoutDb(String nickName, String userLoggedIn) throws UserNotFoundException, NotPermittedException {
        if(nickName == null)
            throw new NullPointerException();

        synchronized (users) {
            User u = users.get(nickName);
            if (u == null)
                throw new UserNotFoundException();

            if(!nickName.equals(userLoggedIn)) //se chi richiede il logout è diverso dall'utente da disconnettere
                throw new NotPermittedException();

            resetUserStatus(nickName);


        }

        return getDbForClients();
    }


    private void resetUserStatus(String nickname) {
        users.get(nickname).setStatus("offline");
        ArrayList<User> listOfUsers = new ArrayList<>(users.values());
        Storage.writeUsersToJson(listOfUsers);

    }


    public boolean isInWorth(String nickName) throws NullPointerException, UserNotFoundException{
        if(nickName == null) throw new NullPointerException();
        synchronized (users){
            if(!users.containsKey(nickName))
                throw new UserNotFoundException();

        }
        return true;

    }


    private ArrayList<UserStatus> getDbForClients(){
        ArrayList<UserStatus> clientsDb = new ArrayList<>();
        Iterator<User> iterator = users.values().iterator();
        while(iterator.hasNext()){
            User user = iterator.next();
            UserStatus uS = new UserStatus(user.getNickName(),user.getStatus());
            clientsDb.add(uS);

        }

        return clientsDb;

    }



}
