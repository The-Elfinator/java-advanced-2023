package info.kgeorgiy.ja.treshchev.bank;

import java.rmi.RemoteException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Abstract class for realization of {@link Person} interface
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public abstract class AbstractPerson implements Person {
    private final String name, surname, passport;
    protected ConcurrentMap<String, Account> accountsOfPerson;

    /**
     * Constructor provides you to create an {@link AbstractPerson} instance
     * with specified {@code name}, {@code surname} and {@code passport}
     * @param name what name should {@link Person} have
     * @param surname what surname should {@link Person} have
     * @param passport what passport should {@link Person} have
     */
    public AbstractPerson(final String name, final String surname, final String passport) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
        this.accountsOfPerson = new ConcurrentHashMap<>();
    }

    /**
     * Returns the name of {@link Person}
     * @return person's name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Returns the surname of {@link Person}
     * @return person's surname
     */
    @Override
    public String getSurname() {
        return this.surname;
    }

    /**
     * Returns the passport of {@link Person}
     * @return person's passport
     */
    @Override
    public String getPassport() {
        return this.passport;
    }

    /**
     * Return a {@link ConcurrentMap} containing all accounts
     * of current {@link Person} with key={@code Account.id} and value={@link Account}
     * @return all accounts of person
     */
    @Override
    public ConcurrentMap<String, Account> getAccountsOfPerson() {
        return this.accountsOfPerson;
    }

    /**
     * Allows you to add a new account of {@link Person} and returns it.
     * If the account has already exists than returns it.
     * @param account what account should be added
     * @return a new {@link Account} or found one if it has been already exists.
     * @throws RemoteException if some RMI error occurred
     */
    @Override
    public Account addAccount(Account account) throws RemoteException {
        if (!this.accountsOfPerson.containsKey(account.getId())) {
            this.accountsOfPerson.put(account.getId(), account);
        }
        return this.accountsOfPerson.get(account.getId());
    }
}
