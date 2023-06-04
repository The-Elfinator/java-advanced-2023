package info.kgeorgiy.ja.treshchev.bank;

import java.io.Serializable;
import java.util.concurrent.ConcurrentMap;

/**
 * Class containing a realization of {@link Person} interface.
 * This is a local person.
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public class LocalPerson extends AbstractPerson implements Serializable {

    /**
     * Constructor for creating a {@link LocalPerson} instance using super class {@link AbstractPerson}
     * @param name what name should person have
     * @param surname what surname should person have
     * @param passport what passport should person have
     * @param accountsOfPerson what accounts person owns
     */
    public LocalPerson(final String name,
                       final String surname,
                       final String passport,
                       final ConcurrentMap<String, Account> accountsOfPerson) {
        super(name, surname, passport);
        this.accountsOfPerson = accountsOfPerson;
    }

}
