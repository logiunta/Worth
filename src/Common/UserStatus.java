package Common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class UserStatus implements Serializable {
    private final String nickName;
    private String status;

    public UserStatus(String nickName){
        this.nickName = nickName;
        status = "offline";
    }

    public UserStatus(String nickName, String status){
        this.nickName = nickName;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNickName() {
        return nickName;
    }

    @Override
    public String toString() {
        return "UserStatus{" +
                "nickName='" + nickName + '\'' +
                ", status='" + status + '\'' +
                '}';
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserStatus that = (UserStatus) o;
        return Objects.equals(nickName, that.nickName) && Objects.equals(status, that.status);
    }



}
