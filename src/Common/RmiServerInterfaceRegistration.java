package Common;

import Exceptions.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RmiServerInterfaceRegistration extends Remote {

    public String signUp(String[] myargs) throws RemoteException, UserAlreadySignedInException;





}
