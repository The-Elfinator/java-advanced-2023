package info.kgeorgiy.ja.treshchev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

/**
 * Class containing abstract realization of {@link HelloClient} interface
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub account</a>)
 */
public abstract class AbstractUDPClient implements HelloClient {

    protected static final String ARGUMENTS_ERROR_MESSAGE = "Error! Illegal arguments! ";
    private static final String GREETING = "Hello, ";
    protected static final int CLIENT_SOCKET_TIMEOUT = 500;

    protected static void parseArgumentsAndRunClient(final String[] args, final HelloClient client) {
        if (args == null || args.length != 5) {
            System.err.println("Usage: " +
                    "java HelloUDPClient <host> <port> <prefix> " +
                    "<count threads sending requests> <count of requests>");
            throw new IllegalArgumentException();
        }
        try {
            final String host = args[0];
            final int portNumber = Integer.parseInt(args[1]);
            final String prefix = args[2];
            final int countSendingThreads = Integer.parseInt(args[3]);
            final int countRequests = Integer.parseInt(args[4]);
            client.run(host, portNumber, prefix, countSendingThreads, countRequests);
        } catch (NumberFormatException e) {
            System.err.println("Expected integer values as arguments! " + e.getMessage());
            throw new IllegalArgumentException();
        }
    }

    protected static String createRequestMessage(final String prefix, final int threadId, final int requestId) {
        return prefix + threadId + "_" + requestId;
    }

    protected static String createResponseMessage(final String requestMessage) {
        return GREETING + requestMessage;
    }

}
