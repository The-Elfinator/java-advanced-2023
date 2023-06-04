package info.kgeorgiy.ja.treshchev.bank;

/**
 * Class containing realization of {@link Person} interface.
 * This is a remote {@link Person}.
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public class RemotePerson extends AbstractPerson {

    /**
     * Constructor for creating a {@link RemotePerson} instance using super class {@link AbstractPerson}
     * @param name what name should person have
     * @param surname what surname should person have
     * @param passport what passport should person have
     */
    public RemotePerson(final String name, final String surname, final String passport) {
        super(name, surname, passport);
    }

}
