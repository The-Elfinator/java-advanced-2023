package info.kgeorgiy.ja.treshchev.bank;

/**
 * Class for realization of remote {@link Account}
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public class RemoteAccount extends AbstractAccount {

    /**
     * Constructor for creating an instance of {@link RemoteAccount} with specified id
     * @param id what id should {@link Account} have
     */
    public RemoteAccount(final String id) {
        super(id, 0);
    }

}
