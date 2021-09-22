package Common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public class User implements Serializable {
    private String nickName;
    private String passw;
    private String status;


    @JsonCreator
    public User(@JsonProperty("nickName") String nickName, @JsonProperty("passw") String passw){
        this.nickName = nickName;
        this.passw = passw;
        this.status = "offline";

    }

    public String getNickName() {
        return this.nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getPassw() {
        return passw;
    }

    public void setPassw(String passw) {
        this.passw = passw;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "User {" +
                "nickName = '" + nickName + '\'' +
                ", passw = '" + passw + '\'' +
                ", status = '" + status + '\'' +
                '}';
    }
}
