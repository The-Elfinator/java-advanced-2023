package info.kgeorgiy.ja.treshchev.bank;

/**
 * Class containing abstract realization of {@link Account} interface.
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub</a>)
 */
public abstract class AbstractAccount implements Account {

    private final String id;
    protected long amount;

    /**
     * Constructor for creating an abstract instance of {@link AbstractAccount} with specified ID
     * @param id what id should account have
     */
    public AbstractAccount(final String id, final long amount) {
        this.id = id;
        this.amount = amount;
    }

    /**
     * Returns an {@link Account} id
     * @return what id account has
     */
    @Override
    public String getId() {
        return this.id;
    }

    /**
     * Returns an {@link Account} amount
     * @return what amount account has now
     */
    @Override
    public synchronized long getAmount() {
        return this.amount;
    }

    /**
     * Sets a new {@link Account} amount
     * @param amount what new amount should account have
     */
    @Override
    public synchronized void setAmount(final long amount) {
        this.amount = amount;
    }

}
