package info.kgeorgiy.ja.treshchev.bank;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Bank interface provides you to make a conversation with a remote bank.
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public interface Bank extends Remote {

    Person createPerson(String name, String surname, String passport) throws RemoteException;

    Person getPerson(String passport, Mode mode) throws RemoteException;

    Account createAccount(String id) throws RemoteException;

    Account getAccount(Person owner, String subId) throws RemoteException;

}
