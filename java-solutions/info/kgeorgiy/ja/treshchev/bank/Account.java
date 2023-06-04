package info.kgeorgiy.ja.treshchev.bank;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Account interface provides you to control the account of {@link Bank}
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public interface Account extends Remote, Serializable {
    String getId() throws RemoteException;

    long getAmount() throws RemoteException;

    void setAmount(long amount) throws RemoteException;
}
