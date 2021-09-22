package Client;

import Common.UserStatus;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

public class LocalDb implements Serializable {
    private ArrayList<UserStatus> usersStatus;

    public LocalDb() {
        usersStatus = new ArrayList<>();
    }

    public void fill(ArrayList<UserStatus> usersStatus){
        this.usersStatus = usersStatus;
    }

    public ArrayList<UserStatus> getUsersStatus(){
        return usersStatus;
    }

    public void clear(){
        usersStatus.clear();
    }

    public ArrayList<String> getListUsers(){
        ArrayList<String> list = new ArrayList<>();
        Iterator<UserStatus> iterator = usersStatus.iterator();
        while(iterator.hasNext()){
            UserStatus uS = iterator.next();
            String user = uS.getNickName();
            list.add(user);
        }

        return list;
    }

    public ArrayList<String> getListOnlineUsers(){
        ArrayList<String> list = new ArrayList<>();
        Iterator<UserStatus> iterator = usersStatus.iterator();
        while(iterator.hasNext()){
            UserStatus uS = iterator.next();
            if(uS.getStatus().equals("online")){
                String user = uS.getNickName();
                list.add(user);
            }
        }

        return list;
    }



}
