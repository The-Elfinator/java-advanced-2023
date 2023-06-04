package info.kgeorgiy.ja.treshchev.bank;

/**
 * Class for realization of local {@link Account}
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public class LocalAccount extends AbstractAccount {

    /**
     * Constructor for creating an instance of {@link LocalAccount} with specified id and amount.
     *
     * @param id     what ID account have
     * @param amount what amount account have now
     */
    public LocalAccount(final String id, final long amount) {
        super(id, amount);
    }

}
