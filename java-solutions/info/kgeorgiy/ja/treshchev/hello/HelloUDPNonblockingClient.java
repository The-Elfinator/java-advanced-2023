package info.kgeorgiy.ja.treshchev.hello;

import info.kgeorgiy.java.advanced.hello.HelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * Class implementing {@link HelloClient} interface.
 *
 * This class provides you to create a request on {@link info.kgeorgiy.java.advanced.hello.HelloServer HelloServer}
 * and receive and print responses in {@link System#out}.
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub account</a>)
 */
public class HelloUDPNonblockingClient extends AbstractUDPClient {

    /**
     * Method creates new nonblocking {@link HelloClient} instance and runs it.
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
            parseArgumentsAndRunClient(args, new HelloUDPNonblockingClient());
        } catch (IllegalArgumentException e) {
            System.err.println(ARGUMENTS_ERROR_MESSAGE);
        }
    }

    /**
     * Method running the client instance.
     * This method allows you to create a request on
     * {@link info.kgeorgiy.java.advanced.hello.HelloServer HelloServer}
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
        final Selector selector;
        try {
            selector = Selector.open();
        } catch (IOException e) {
            System.err.println("Couldn't open selector: " + e.getMessage());
            return;
        }
        final SocketAddress serverAddress;
        try {
            serverAddress = new InetSocketAddress(InetAddress.getByName(host), portNumber);
        } catch (UnknownHostException e) {
            System.err.println("Unknown host was found! " + e.getMessage());
            return;
        }

        DatagramChannel datagramChannel;

        try {
            for (int i = 1; i <= countSendingThreads; i++) {
                datagramChannel = DatagramChannel.open();
                datagramChannel.configureBlocking(false);
                datagramChannel.connect(serverAddress);
                datagramChannel.register(selector, SelectionKey.OP_WRITE, new Context(i, countRequests, prefix));
            }
            while (!Thread.interrupted() && selector.isOpen() && !selector.keys().isEmpty()) {
                if (!processRequest(prefix, selector, serverAddress)) return;
            }

        } catch (ClosedChannelException e) {
            System.err.println("Channel is already closed: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Couldn't open a datagram channel: " + e.getMessage());
        } catch (AlreadyBoundException e) {
            System.err.println("Address is already bound: " + e.getMessage());
        } catch (UnsupportedAddressTypeException e) {
            System.err.println("Address is unsupported: " + e.getMessage());
        } catch (SecurityException e) {
            System.err.println("Security error occurred: " + e.getMessage());
        }
    }

    private boolean processRequest(String prefix, Selector selector, SocketAddress serverAddress) throws IOException {
        try {
            if (selector.select(CLIENT_SOCKET_TIMEOUT) == 0) {
                for (SelectionKey k : selector.keys()) {
                    k.interestOps(SelectionKey.OP_WRITE);
                }
            }
        } catch (IOException e) {
            System.err.println("Error selecting: " + e.getMessage());
            return false;
        }
        for (final Iterator<SelectionKey> i = selector.selectedKeys().iterator(); i.hasNext(); ) {
            final SelectionKey key = i.next();
            final Context context = (Context) key.attachment();
            final String messageToSend = createRequestMessage(prefix,
                    context.getThreadId(), context.getRequestNumber());
            final String messageToReceive = createResponseMessage(messageToSend);
            try {
                if (key.isReadable()) {
                    readResponse(key, context, messageToReceive);
                }
                try {
                    if (key.isWritable()) {
                        writeRequest(serverAddress, key, messageToSend);
                    }
                } catch (CancelledKeyException ignored) {

                }
            } finally {
                i.remove();
            }
        }
        return true;
    }

    private void writeRequest(SocketAddress serverAddress, SelectionKey key, String messageToSend) {
        final ByteBuffer buffer = ByteBuffer.wrap(messageToSend.getBytes(StandardCharsets.UTF_8));
        final DatagramChannel channel = (DatagramChannel) key.channel();
        try {
            channel.send(buffer, serverAddress);
        } catch (IOException e) {
            System.err.println("IOException when send request");
        }
        key.interestOps(SelectionKey.OP_READ);
    }

    private void readResponse(SelectionKey key, Context context, String messageToReceive) throws IOException {
        final DatagramChannel channel = (DatagramChannel) key.channel();
        final ByteBuffer buffer = ByteBuffer.allocate(channel.socket().getReceiveBufferSize()); // :NOTE: done
        try {
            channel.receive(buffer);
        } catch (IOException e) {
            System.err.println("Error occurred while receiving data: " + e.getMessage());
        }
        buffer.flip();
        final String requestMessage = StandardCharsets.UTF_8.decode(buffer).toString();
        buffer.clear();
        if (requestMessage.equals(messageToReceive)) {
            context.increment();
            System.out.println(requestMessage);
        }
        if (context.getRequestNumber() <= context.getCountRequests()) {
            key.interestOps(SelectionKey.OP_WRITE);
        } else {
            channel.close();
        }
    }


}
