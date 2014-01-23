package com.reinventedcode.jyslog;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public final class SocketAddressUtils {
    private static final byte[] IPV4_LOCALHOST = new byte[] {127, 0, 0, 1};

    private SocketAddressUtils() {
    }

    public static SocketAddress localAddress(final int port)
        throws IOException
    {
        return new InetSocketAddress(InetAddress.getByAddress(IPV4_LOCALHOST),
            port);
    }
}

