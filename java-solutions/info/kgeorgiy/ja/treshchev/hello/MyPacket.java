package info.kgeorgiy.ja.treshchev.hello;

import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class MyPacket {
    private final SocketAddress address;
    private final ByteBuffer buffer;

    public MyPacket(final ByteBuffer buffer, final SocketAddress address) {
        this.buffer = buffer;
        this.address = address;
    }

    public SocketAddress getAddress() {
        return address;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

}
