package info.kgeorgiy.ja.treshchev.bank;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Public class containing a realization of {@link Bank} interface.
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public class RemoteBank implements Bank {

    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> personAccounts = new ConcurrentHashMap<>();

    /**
     * Constructor creating bank on specified port.
     * @param port which port should {@link Bank} be binded to
     */
    public RemoteBank(final int port) {
        this.port = port;
    }

    /**
     * Method allows you to create a {@link Person} with their data.
     *
     * If the {@link Person} with specified data already exists
     * than returns this Person instance of {@link RemotePerson} by using {@link RemoteBank#getPerson(String)} method.
     * Otherwise, creates the person with specified data and returns it like instance of {@link RemotePerson}
     * Throws {@link IllegalArgumentException} if some arguments are prohibited to use
     * @param name what name of {@link Person}
     * @param surName what surname of {@link Person}
     * @param passport what passport code {@link Person} has
     * @return Person that has been created or found in users already had been existing
     * @throws RemoteException if some RMI error occurred
     */
    public synchronized Person createPerson(final String name, final String surName, final String passport)
            throws RemoteException {
        if (name == null ||  name.length() == 0 ||
                surName == null || surName.isBlank() ||
                passport == null || passport.isBlank()) {
            throw new IllegalArgumentException("Required notnull and not empty values!");
        }
        final RemotePerson person = new RemotePerson(name, surName, passport);
        if (this.persons.putIfAbsent(passport, person) == null) {
            UnicastRemoteObject.exportObject(person, this.port);
            return person;
        }
        return this.getPerson(passport);
    }

    private synchronized Person getPerson(final String passport) {
        return this.persons.get(passport);
    }

    /**
     * Method provides you to get a person by his passport code in specified {@link Mode}.
     *
     * Required not null passport code.
     * Returns instance of {@link RemotePerson} if {@link Mode} was specified like {@link Mode#REMOTE}.
     * Returns instance of {@link LocalPerson} if {@link Mode} was specified like {@link Mode#LOCAL}.
     * Otherwise, returns null.
     *
     * @param passport passport code of {@link Person} you want to get
     * @param mode what instance should {@link Person} be: {@link RemotePerson} or {@link LocalPerson}
     * @return {@link Person} that has been found by passport code
     * @throws RemoteException if some RMI error occurred.
     */
    public synchronized Person getPerson(final String passport, final Mode mode) throws RemoteException {
        if (passport == null) {
            throw new IllegalArgumentException("Required notnull value!");
        }
        final RemotePerson remotePerson = (RemotePerson) this.getPerson(passport);
        if (mode == Mode.REMOTE) {
            return remotePerson;
        }
        final String name = remotePerson.getName();
        final String surname = remotePerson.getSurname();
        final ConcurrentMap<String, Account> personAccounts = new ConcurrentHashMap<>();
        for (Account account : remotePerson.getAccountsOfPerson().values()) {
            LocalAccount localAccount = new LocalAccount(account.getId(), account.getAmount());
            personAccounts.put(localAccount.getId(), localAccount);
        }
        return new LocalPerson(name, surname, passport, personAccounts);
    }

    /**
     * Method allows you to create an account using specified ID.
     *
     * ID should be like {@code <passport>:<subId>}. Throws {@link IllegalArgumentException}
     * if argument was null or doesn't match the pattern,
     * Creates a remote account of specified id with {@code amount=0} and returns it or
     * just returns an account if it has been already exist using {@link RemoteBank#getAccount(Person, String)}
     * @param id what id should an account have
     * @return an {@link Account} that has been created or found
     * @throws RemoteException if some RMI error occurred
     */
    @Override
    public synchronized Account createAccount(final String id) throws RemoteException {
        if (id == null)
            throw new IllegalArgumentException("Required not null id!");
        final String[] tokens = id.split(":");
        if (tokens.length != 2 || tokens[1] == null || tokens[1].length() == 0)
            throw new IllegalArgumentException("Expected id as <passport>:<subId>");

        final String passport = tokens[0];
        final String subId = tokens[1];
        if (this.accounts.containsKey(id)) {
            return this.accounts.get(id);
        }

        final Account account = new RemoteAccount(id);
        UnicastRemoteObject.exportObject(account, this.port);
        this.accounts.put(id, account);
        if (!this.personAccounts.containsKey(passport)) {
            this.personAccounts.put(passport, ConcurrentHashMap.newKeySet());
        }
        final Person creator = this.persons.get(passport);
        creator.addAccount(account);
        this.personAccounts.get(passport).add(subId);
        return account;
    }

    /**
     * Method provides you to get an account with specified owner and subId.
     *
     * Founds an {@link Account} using {@link Person} and {@code subID} that account has.
     * Returns null if there was no account with specified owner and subID.
     * Throws {@link IllegalArgumentException} if arguments are null or {@code subID} is empty
     * @param owner what {@link Person} should own an account
     * @param subId what subID account has, account id is {@code <passport>:<subId>}
     * @return {@link Account} if found
     * @throws RemoteException if some RMI error occurred
     */
    @Override
    public synchronized Account getAccount(final Person owner, final String subId) throws RemoteException {
        if (owner == null || subId == null || subId.length() == 0) {
            throw new IllegalArgumentException("Required notnull and not empty values!");
        }
        final String accountId = owner.getPassport() + ":" + subId;
        if (owner instanceof LocalPerson) {
            return owner.getAccountsOfPerson().get(accountId);
        }
        return this.accounts.get(accountId);
    }
}
