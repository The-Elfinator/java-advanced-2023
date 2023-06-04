package info.kgeorgiy.ja.treshchev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class implementing {@link HelloClient} interface.
 *
 * This class provides you to create a request on {@link info.kgeorgiy.java.advanced.hello.HelloServer HelloServer}
 * and receive and print responses in {@link System#out}.
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub account</a>)
 */
public class HelloUDPClient extends AbstractUDPClient {

    /**
     * Method creates new {@link HelloClient} instance and runs it.
     * Arguments of command line should be {@code host} where the server is working,
     * {@code port} where to send requests,
     * {@code prefix} what message should be sent to the server
     * (requests are formed according to the scheme {@code prefix + threadId + "_" + requestId}
     * where {@code threadId} is number of thread that sends a request,
     * and {@code requestId} is number of request in this thread),
     * {@code countThreads} how many threads should be created to complete request and
     * {@code countRequests} how many requests should be sent inside 1 thread.
     * @param args command line arguments
     */
    public static void main(final String[] args) {
        try {
            parseArgumentsAndRunClient(args, new HelloUDPClient());
        } catch (IllegalArgumentException e) {
            System.err.println(ARGUMENTS_ERROR_MESSAGE);
        }
    }

    /**
     * Method running the client instance.
     * This method allows you to create a request on {@link info.kgeorgiy.java.advanced.hello.HelloServer HelloServer}
     * and receive a response from it.
     * @param host where the server locates
     * @param portNumber where to send a request
     * @param prefix what message should be sent to the server
     *               (requests are formed according to the scheme {@code prefix + threadId + "_" + requestId}
     *               where {@code threadId} is number of thread that sends a request,
     *               and {@code requestId} is number of request in this thread)
     * @param countSendingThreads how many threads should send requests
     * @param countRequests how many requests should each thread send
     */
    @Override
    public void run(final String host,
                    final int portNumber,
                    final String prefix,
                    final int countSendingThreads,
                    final int countRequests) {
        final ExecutorService requesters = Executors.newFixedThreadPool(countSendingThreads);
        final InetAddress serverAddress;
        try {
             serverAddress = InetAddress.getByName(host);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host was found! " + e.getMessage());
            return;
        }
        for (int i = 1; i <= countSendingThreads; i++) {
            requesters.submit(requesterTask(serverAddress, portNumber, countRequests, prefix, i));
        }

        finish(requesters);
    }

    private Runnable requesterTask(final InetAddress serverAddress,
                                   final int portNumber,
                                   final int countRequests,
                                   final String prefix,
                                   final int threadId) {
        return () -> {
            try (final DatagramSocket clientSocket = new DatagramSocket()) {
                clientSocket.setSoTimeout(CLIENT_SOCKET_TIMEOUT);
                completeRequest(clientSocket, serverAddress, portNumber, countRequests, prefix, threadId);
            } catch (SocketException e) {
                System.err.println("Couldn't create client socket! " + e.getMessage());
            }
        };
    }

    private void completeRequest(final DatagramSocket clientSocket,
                                 final InetAddress serverAddress,
                                 final int portNumber,
                                 final int countRequests,
                                 final String prefix,
                                 final int threadId) {
        for (int requestId = 1; requestId <= countRequests; requestId++) {
            final String requestMessage = createRequestMessage(prefix, threadId, requestId);
            final String responseMessage = createResponseMessage(requestMessage);
            while (!Thread.interrupted() && !clientSocket.isClosed()) {
                try {
                    sendPacket(clientSocket, serverAddress, portNumber, requestMessage);
                    if (receivePacket(clientSocket, responseMessage)) {
                        break;
                    }
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    private void sendPacket(final DatagramSocket clientSocket,
                            final InetAddress serverAddress,
                            final int portNumber,
                            final String requestMessage) throws IOException {
        final DatagramPacket packet = getRequestPacket(serverAddress, portNumber, requestMessage);
        try {
            clientSocket.send(packet);
        } catch (IOException e) {
            throw new IOException("Couldn't send packet to the server! " + e.getMessage());
        }
    }

    private DatagramPacket getRequestPacket(final InetAddress serverAddress,
                                            final int portNumber,
                                            final String requestMessage) {
        final byte[] buffer = requestMessage.getBytes(StandardCharsets.UTF_8);
        return new DatagramPacket(buffer, buffer.length, serverAddress, portNumber);

    }

    private boolean receivePacket(final DatagramSocket clientSocket, final String expected) throws IOException {
        final DatagramPacket packet = new DatagramPacket(new byte[clientSocket.getReceiveBufferSize()],
                clientSocket.getReceiveBufferSize());
        try {
            clientSocket.receive(packet);
        } catch (IOException e) {
            throw new IOException("Couldn't receive a packet from the server! " + e.getMessage());
        }
        return checkResponse(packet, expected);
    }

    private boolean checkResponse(final DatagramPacket packet, final String expected) {
        final String responseMessage = new String(packet.getData(),
                packet.getOffset(), packet.getLength(), StandardCharsets.UTF_8);
        if (responseMessage.equals(expected)) {
            System.out.println(responseMessage);
            return true;
        }
        return false;
    }

    private void finish(final ExecutorService requesters) {
        ClosingTool.close(requesters);
    }

}
