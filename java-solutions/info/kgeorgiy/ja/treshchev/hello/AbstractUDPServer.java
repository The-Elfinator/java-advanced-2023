package info.kgeorgiy.ja.treshchev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class containing abstract realization of {@link HelloServer} interface
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub account</a>)
 */
public abstract class AbstractUDPServer implements HelloServer {

    protected static final String GREETING = "Hello, ";
    protected static final String ILLEGAL_ARGUMENTS_ERROR = "Error! Illegal arguments found! ";

    protected ExecutorService processors;

    protected static void parseArgsAndRunServer(final String[] args, final HelloServer server) {
        if (args == null || args.length != 2) {
            System.err.println("Usage: java " + server.getClass().getSimpleName() +
                    " <port number> <count of threads processing requests>");
            throw new IllegalArgumentException();
        }
        try {
            final int portNumber = Integer.parseInt(args[0]);
            final int countProcessors = Integer.parseInt(args[1]);
            server.start(portNumber, countProcessors);
        } catch (NumberFormatException e) {
            System.err.println("Expected integer values as arguments!  " + e.getMessage());
            throw new IllegalArgumentException();
        }
    }

    protected void createProcessors(final int threadsCount) {
        this.processors = Executors.newFixedThreadPool(threadsCount);
    }

    protected void closeProcessors() {
        ClosingTool.close(this.processors);
    }

}
