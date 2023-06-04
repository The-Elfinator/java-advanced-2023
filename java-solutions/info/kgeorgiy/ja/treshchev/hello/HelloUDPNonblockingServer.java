package info.kgeorgiy.ja.treshchev.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Class extending {@link AbstractUDPServer} class.
 * <p>
 * This class provides creating a nonblocking {@link HelloServer} using specified port
 * and count of threads processing requests.
 * On every request that server has been sent to the server
 * response will be {@code "Hello, " + requestMessage},
 * where {@code requestMessage} is the message receiving from client.
 * </p>
 *
 * @author artem (<a href="https://github.com/The-Elfinator">GitHub account</a>)
 */
public class HelloUDPNonblockingServer extends AbstractUDPServer {

    /**
     * Method creates the new {@link HelloServer} instance and starts its work.
     * Arguments of command line should be {@code port} where to receive requests
     * and {@code count} threads to process requests.
     *
     * @param args arguments of command line
     */
    public static void main(final String[] args) {
        try {
            parseArgsAndRunServer(args, new HelloUDPNonblockingServer());
        } catch (IllegalArgumentException e) {
            System.err.println(ILLEGAL_ARGUMENTS_ERROR);
        }
    }

    private static final int QUEUE_SIZE = 4096 * 4096;

    private Selector selector;
    private DatagramChannel datagramChannel;
    private final BlockingQueue<MyPacket> queueToSend = new ArrayBlockingQueue<>(QUEUE_SIZE);
    private ExecutorService dispatcher;

    /**
     * Starts the work of the server.
     * Method allows you to create server socket on specified port and
     * fixed thread pool of specified count of threads.
     *
     * @param port         to which port socket should be binded
     * @param threadsCount how many threads could be created to process requests
     */
    @Override
    public void start(final int port, final int threadsCount) {
        try {
            this.selector = Selector.open();
        } catch (IOException e) {
            System.err.println("Couldn't open a selector: " + e.getMessage());
            return;
        }
        SocketAddress address = new InetSocketAddress(port);
        if (!this.createChannel(address)) return;
        try {
            this.datagramChannel.socket().getReceiveBufferSize();
        } catch (SocketException e) {
            System.err.println("Socket error occurred: " + e.getMessage());
            return;
        }
        createProcessors(threadsCount);
        this.dispatcher = Executors.newSingleThreadExecutor();
        this.dispatcher.submit(dispatcherTask());
    }

    private boolean createChannel(SocketAddress address) {
        try {
            this.datagramChannel = DatagramChannel.open();
            this.datagramChannel.configureBlocking(false);
            this.datagramChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            this.datagramChannel.bind(address);
            this.datagramChannel.register(this.selector, SelectionKey.OP_READ);
        } catch (ClosedChannelException e) {
            System.err.println("Channel is already closed: " + e.getMessage());
            return false;
        } catch (IOException e) {
            System.err.println("Couldn't open a datagram channel: " + e.getMessage());
            return false;
        } catch (AlreadyBoundException e) {
            System.err.println("Address is already bound: " + e.getMessage());
            return false;
        } catch (UnsupportedAddressTypeException e) {
            System.err.println("Address is unsupported: " + e.getMessage());
            return false;
        } catch (SecurityException e) {
            System.err.println("Security error occurred: " + e.getMessage());
            return false;
        }
        return true;
    }

    private Runnable dispatcherTask() {
        return () -> {
            while (!Thread.interrupted() && !this.datagramChannel.socket().isClosed()) {
                try {
                    this.selector.select();
                } catch (IOException e) {
                    System.err.println("Error selecting: " + e.getMessage());
                    return;
                }
                for (final Iterator<SelectionKey> i = this.selector.selectedKeys().iterator(); i.hasNext(); ) {
                    final SelectionKey key = i.next();
                    try {
                        process(key);
                    } finally {
                        i.remove();
                    }
                }

            }
        };
    }

    private void process(final SelectionKey key) {
        DatagramChannel channel = (DatagramChannel) key.channel();
        if (key.isReadable()) {
            this.processors.submit(readingTask(key, channel));
        }
        if (key.isWritable()) {
            this.processors.submit(writingTask(key, channel));
        }
    }

    private static final boolean log = false;

    private static void logError(final String message) {
        if (log) {
            System.err.println(message);
        }
    }

    private Runnable readingTask(final SelectionKey key, final DatagramChannel channel) {
        return () -> {
            final ByteBuffer buffer;
            try {
                buffer = ByteBuffer.allocate(channel.socket().getReceiveBufferSize());
            } catch (SocketException e) {
                logError("UDP error occurred!");
                return;
            }

            final SocketAddress address;
            try {
                address = channel.receive(buffer);
            } catch (IOException e) {
                logError("Error occurred while receiving data: " + e.getMessage());
                buffer.clear();
                return;
            }
            buffer.flip();
            final String requestMessage = StandardCharsets.UTF_8.decode(buffer).toString();
            buffer.clear();
            final String responseMessage = GREETING + requestMessage;
            final byte[] responseBuffer = responseMessage.getBytes(StandardCharsets.UTF_8);
            MyPacket myPacket = new MyPacket(ByteBuffer.wrap(responseBuffer), address);
            if (this.queueToSend.isEmpty()) {
                this.queueToSend.add(myPacket);
                key.interestOpsOr(SelectionKey.OP_WRITE);
                this.selector.wakeup();
            } else {
                this.queueToSend.add(myPacket);
            }
        };
    }

    private Runnable writingTask(final SelectionKey key, final DatagramChannel channel) {
        return () -> {
            MyPacket myPacket = this.queueToSend.poll();
            if (myPacket == null) {
                key.interestOpsAnd(~SelectionKey.OP_WRITE);
                this.selector.wakeup();
                return;
            }
            final ByteBuffer buffer = myPacket.getBuffer();
            try {
                channel.send(buffer, myPacket.getAddress());
            } catch (IOException e) {
                logError("Couldn't send a myPacket: " + e.getMessage());
            } finally {
                buffer.clear();
            }
        };
    }

    /**
     * Closing the server.
     * This method allows you to close the server's selector and datagramChannel
     * to not receive any requests
     * and to shut down all threads processing and sending response to a client
     * with a help of {@link ClosingTool} class.
     */
    @Override
    public void close() {
        try {
            this.selector.close();
            this.datagramChannel.close();
        } catch (IOException e) {
            System.err.println("Couldn't close: " + e.getMessage());
        }
        ClosingTool.close(this.dispatcher);
        closeProcessors();
    }
}
