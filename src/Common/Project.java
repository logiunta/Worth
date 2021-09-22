package Common;

import Exceptions.CardAlreadyExistException;
import Exceptions.CardNotFoundException;
import Exceptions.ListMisMatchException;
import Exceptions.ProjectAlreadyAddedException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


import javax.smartcardio.CardNotPresentException;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Project implements Serializable {
    public static final String TodoState = "todo";
    public static final String InProgressState = "inprogress";
    public static final String ToBeRevisedState = "toberevised";
    private static final String DoneState = "done";
    private final String projectName;
    private final ArrayList<String> users;
    private final ArrayList<Card> cardsList;
    private final ArrayList<String> todoList;
    private final ArrayList<String> inProgessList;
    private final ArrayList<String> toBeRevisedList;
    private final ArrayList<String> doneList;
    private final String multiAddress;
    private File dir;
    private File fileInfo;
    private File cardsDir;


    @JsonCreator
    public Project(@JsonProperty("projectName") String projectName,
                   @JsonProperty("creatorUser") String user,@JsonProperty("ip") String multiAddress){
        this.projectName = projectName;
        this.multiAddress = multiAddress;
        users = new ArrayList<>();
        users.add(user); //utente che crea il progetto
        todoList = new ArrayList<>();
        toBeRevisedList = new ArrayList<>();
        doneList = new ArrayList<>();
        inProgessList = new ArrayList<>();
        cardsList = new ArrayList<>();
        dir = new File("./data/Projects/" + projectName);
        fileInfo = new File("./data/Projects/" + projectName + "/" + projectName + "Info.json");
        cardsDir = new File("./data/Projects/" + projectName + "/" + "Cards");

        if (!dir.exists()) {
            dir.mkdir();
            if(!fileInfo.exists()) {
                try {
                    fileInfo.createNewFile();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(!cardsDir.exists()){
                cardsDir.mkdir();
            }
        }

    }

    public void addMember(String nickName){
        users.add(nickName);
    }

    public boolean addCard(Card card){
        Card c = findCard(card.getName());
        if(c == null){
            todoList.add(card.getName());
            cardsList.add(card);
            return true;
        }
        return false;

    }

    public void restoreCard(Card card){
        String lastState = card.getLastState();
        switch (lastState){
            case TodoState:
                todoList.add(card.getName());
                break;
            case ToBeRevisedState:
                toBeRevisedList.add(card.getName());
                break;
            case InProgressState:
                inProgessList.add(card.getName());
                break;
            case DoneState:
                doneList.add(card.getName());
                break;

        }
        cardsList.add(card);
    }

    public Card findCard(String cardName){
        for(Card card : cardsList){
            if(card.getName().equals(cardName))
                return card;
        }
        return null;

    }

    public Card moveCard(String cardName,String listaPartenza, String listaDestinazione) throws CardNotFoundException, ListMisMatchException {
        Card card = findCard(cardName);
        if(card == null) throw new CardNotFoundException();
        String state = card.getLastState();
        if(!state.equals(listaPartenza)) throw new CardNotFoundException();
        boolean switchRes = false;

        switch (state){
            case TodoState:
                if(listaDestinazione.equals(InProgressState)) {
                    card.updateLastStatus(listaDestinazione);
                    inProgessList.add(cardName);
                    todoList.remove(cardName);
                    switchRes = true;
                }
                break;

            case InProgressState:
                if(listaDestinazione.equals(ToBeRevisedState)) {
                    card.updateLastStatus(listaDestinazione);
                    toBeRevisedList.add(cardName);
                    inProgessList.remove(cardName);
                    switchRes = true;
                }
                else if(listaDestinazione.equals(DoneState)){
                    card.updateLastStatus(listaDestinazione);
                    doneList.add(cardName);
                    inProgessList.remove(cardName);
                    switchRes = true;

                }
                break;

            case ToBeRevisedState:
                if(listaDestinazione.equals(DoneState)) {
                    card.updateLastStatus(listaDestinazione);
                    doneList.add(cardName);
                    toBeRevisedList.remove(cardName);
                    switchRes = true;
                }
                else if (listaDestinazione.equals(InProgressState)){
                    card.updateLastStatus(listaDestinazione);
                    inProgessList.add(cardName);
                    toBeRevisedList.remove(cardName);
                    switchRes = true;
                }
                break;

            case DoneState:
                break;

            }
            if(!switchRes) throw new ListMisMatchException();


            return card;
    }


    public String getProjectName() {
        return projectName;
    }


    public String getMultiAddress(){
        return multiAddress;
    }

    public ArrayList<String> getUsers(){
        return users;
    }

    public boolean allCardsDone(){

        return (todoList.size() == 0 && inProgessList.size() == 0 && toBeRevisedList.size() == 0);

    }

    public boolean listExists(String lista){
        return lista.equals(ToBeRevisedState) || lista.equals(TodoState) || lista.equals(InProgressState) || lista.equals(DoneState);
    }

    public ArrayList<String> getCardsList(){
        ArrayList<String> list = new ArrayList<>();
        for(Card c : cardsList){
            list.add(c.getName());
        }
        return list;
    }






}


