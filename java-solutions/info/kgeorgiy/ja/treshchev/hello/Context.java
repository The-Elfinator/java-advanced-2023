package info.kgeorgiy.ja.treshchev.hello;

/**
 * Context for channels.
 *
 * This class provides a special context for {@link java.nio.channels.DatagramChannel}
 * containing {@code threadId} and {@code requestId}
 */
public class Context {
    private final int threadId;
    private final int countRequests;
    private final String prefix;
    private int requestNumber;

    /**
     * Constructor for creating instance of {@link Context}
     * @param threadId what number channel has
     * @param countRequests how many requests should be sent
     * @param prefix what message should be sent
     */
    public Context(final int threadId, final int countRequests, final String prefix) {
        this.threadId = threadId;
        this.countRequests = countRequests;
        this.prefix = prefix;
        this.requestNumber = 1;
    }

    /**
     * Getting number of the channel
     * @return integer number of the channel
     */
    public int getThreadId() {
        return threadId;
    }

    /**
     * Getting count of requests that specified channel should send
     * @return integer number of how many requests channel should send
     */
    public int getCountRequests() {
        return countRequests;
    }

    /**
     * Getting prefix of the message
     * @return the {@link String} prefix of the message that should be sent
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Getting request number in this channel
     * @return what current number request has
     */
    public int getRequestNumber() {
        return requestNumber;
    }

    /**
     * Method to increase a current number of request
     */
    public void increment() {
        this.requestNumber++;
    }
}
