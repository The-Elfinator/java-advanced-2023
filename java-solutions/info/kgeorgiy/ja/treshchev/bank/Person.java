package info.kgeorgiy.ja.treshchev.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Person interface provides you to control users of {@link Bank}
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public interface Person extends Remote {

    String getName() throws RemoteException;

    String getSurname() throws RemoteException;

    String getPassport() throws RemoteException;

    Map<String, Account> getAccountsOfPerson() throws RemoteException;

    Account addAccount(Account account) throws RemoteException;
}
