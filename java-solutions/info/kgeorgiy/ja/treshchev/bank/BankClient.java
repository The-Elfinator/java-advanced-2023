package info.kgeorgiy.ja.treshchev.bank;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * Client application of {@link Bank}
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public final class BankClient {

    /**
     * Method provides you to run a client side of {@link Bank}.
     *
     * This method required arguments of command line like
     * {@code <name> <surname> <passport> <subId> <diff amount>}.
     * This method creates a new {@link Person} if it wasn't in bank before, founds an {@link Account}
     * with id={@code <passport>:<subId>} and sets new amount of {@link Account}
     * using {@code <old amount> + <diff amount>}
     * @param args what arguments of command line expected, see usage.
     */
    public static void main(final String[] args) {
        if (args == null || args.length != 5) {
            System.err.println("Usage: java BankClient <name> <surname> <passport> <subId> <diff amount>");
            return;
        }
        final String name = args[0];
        final String surname = args[1];
        final String passport = args[2];
        final String subId = args[3];
        final long diff;
        try {
            diff = Long.parseLong(args[4]);
        } catch (NumberFormatException e) {
            System.err.println("Expected longInteger value as diff amount! " + e.getMessage());
            return;
        }

        completeOperation(name, surname, passport, subId, diff);
    }

    private static void completeOperation(final String name,
                                          final String surname,
                                          final String passport,
                                          final String subId,
                                          final long diff) {
        try {
            final Bank bank = getBank();
            if (bank == null) {
                return;
            }
            Person person = getPerson(bank, name, surname, passport);
            if (person == null) {
                return;
            }
            Account account = getAccount(bank, person, passport, subId);
            if (account == null) {
                return;
            }
            changeAmount(account, diff);
        } catch (RemoteException e) {
            System.err.println("Remote exception caught: " + e.getMessage());

        }
    }

    private static Bank getBank() throws RemoteException {
        try {
            LocateRegistry.getRegistry(8080);
            return (Bank) Naming.lookup("//localhost:8080/bank");
        } catch (NotBoundException e) {
            System.err.println("Bank not bound: " + e.getMessage());
            return null;
        } catch (MalformedURLException e) {
            System.err.println("Malformed URL!");
            return null;
        }
    }

    private static Person getPerson(final Bank bank,
                     final String name,
                     final String surname,
                     final String passport) throws RemoteException {
        try {
            Person person = bank.getPerson(passport, Mode.REMOTE);
            if (person == null) {
                person = bank.createPerson(name, surname, passport);
            } else {
                if (!person.getName().equals(name) || !person.getSurname().equals(surname)) {
                    System.err.println("Incorrect name or surname!");
                    return null;
                }
            }
            return person;
        } catch (IllegalArgumentException e) {
            System.err.println("Illegal argument: " + e.getMessage());
            return null;
        }
    }

    private static Account getAccount(final Bank bank,
                                      final Person person,
                                      final String passport,
                                      final String subId) throws RemoteException {
        try {
            Account account = bank.getAccount(person, subId);
            if (account == null) {
                account = bank.createAccount(passport + ":" + subId);
            }
            return account;
        } catch (IllegalArgumentException e) {
            System.err.println("Illegal argument: " + e.getMessage());
            return null;
        }
    }

    private static void changeAmount(final Account account, final long diff) throws RemoteException {
        System.out.println("Account is: " + account.getId());
        System.out.println("Current balance: " + account.getAmount());
        account.setAmount(account.getAmount() + diff);
        System.out.println("Balance has changed on " + diff + ". Current balance: " + account.getAmount());
    }

}
