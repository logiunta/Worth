package Common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;

public class Card implements Serializable {
    private final String name;
    private final String description;
    private final ArrayList<String> history;
    private String lastState;


    @JsonCreator
    public Card(@JsonProperty("name") String name,@JsonProperty("description") String description){
        this.name = name;
        this.description = description;
        history = new ArrayList<>();
        history.add(Project.TodoState);
        lastState = getLastState();

    }


    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ArrayList<String> getHistory() {
        return history;
    }

    public String getLastState() {
        return history.get(history.size()-1);

    }

    public void updateLastStatus(String value){
        if(!lastState.equals(value)) {
            lastState = value;
            history.add(value);
        }

    }

    @Override
    public String toString() {
        return "Card{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", history=" + history +
                ", lastState='" + lastState + '\'' +
                '}';
    }
}
