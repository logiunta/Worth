package Common;

import java.awt.*;

public class Message {
    private final String sender;
    private final String message;

    public Message(String sender,String message){
        this.message = message;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }
    public String getSender(){
        return sender;
    }
}
