package info.kgeorgiy.ja.treshchev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

/**
 * Class extending {@link AbstractUDPServer} class.
 * <p>
 * This class provides creating {@code HelloServer} using specified port
 * and count of threads processing requests.
 * On every request that server has been sent to the server
 * response will be {@code "Hello, " + requestMessage},
 * where {@code requestMessage} is the message receiving from client.
 * </p>
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub account</a>)
 */
public class HelloUDPServer extends AbstractUDPServer {

    private static final int BUFFER_SIZE = 65536;

    private DatagramSocket serverSocket;

    /**
     * Method creates the new {@link HelloServer} instance and starts its work.
     * Arguments of command line should be {@code port} where to receive requests
     * and {@code count} threads to process requests.
     *
     * @param args arguments of command line
     */
    public static void main(final String[] args) {
        try {
            parseArgsAndRunServer(args, new HelloUDPServer());
        } catch (IllegalArgumentException e) {
            System.err.println(ILLEGAL_ARGUMENTS_ERROR);
        }
    }

    /**
     * Starts the work of the server.
     * Method allows you to create server socket on specified port and
     * fixed thread pool of specified count of threads.
     *
     * @param portNumber      to which port socket should be binded
     * @param countProcessors how many threads could be created to process requests
     */
    @Override
    public void start(final int portNumber, final int countProcessors) {
        createProcessors(countProcessors);
        try {
            this.serverSocket = new DatagramSocket(portNumber);
        } catch (IOException e) {
            System.err.println("Couldn't create server socket!  " + e.getMessage());
            return;
        }
        for (int i = 0; i < countProcessors; i++) {
            this.processors.submit(processorTask());
        }
    }

    private Runnable processorTask() {
        return () -> {
            while (!this.serverSocket.isClosed()) {
                processPacket();
            }
        };
    }

    private void processPacket() {
        DatagramPacket packet = new DatagramPacket(new byte[BUFFER_SIZE], BUFFER_SIZE);
        try {
            this.serverSocket.receive(packet);
        } catch (IOException e) {
            errorReceiveOrSendMessage("receive", e);
            return;
        }
        packet = createResponse(packet);
        try {
            this.serverSocket.send(packet);
        } catch (IOException e) {
            errorReceiveOrSendMessage("send", e);
        }
    }

    private void errorReceiveOrSendMessage(final String error, final IOException e) {
        System.err.printf(
                "Error communication with a client! Couldn't %s a packet!%s%s%n",
                error, System.lineSeparator(), e.getMessage()
        );
    }

    private DatagramPacket createResponse(final DatagramPacket packet) {
        final byte[] buffer = getBuffer(packet);
        final InetAddress address = packet.getAddress();
        final int port = packet.getPort();
        return new DatagramPacket(buffer, buffer.length, address, port);
    }

    private byte[] getBuffer(final DatagramPacket packet) {
        return (GREETING + new String(
                packet.getData(), packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8))
                .getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Closing the server.
     * This method allows you to close the server socket to not receiving any requests
     * and to shut down all threads processing and sending response to a client
     * with a help of {@link ClosingTool} class.
     */
    @Override
    public void close() {
        this.serverSocket.close();
        closeProcessors();
    }
}
