package com.reinventedcode.jyslog;

import java.io.IOException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;

import java.util.function.Supplier;

public class UdpSyslog extends AbstractSyslog {
    private static final int SEND_BUFFER_SIZE = 1048576;
    private static final byte[] IPV4_LOCALHOST = new byte[] {127, 0, 0, 1};

    public static final int DEFAULT_PORT = 514;

    private final DatagramChannel channel;

    public UdpSyslog() throws IOException {
        this(Rfc5424FormatterSupplier.INSTANCE);
    }

    public UdpSyslog(final Supplier<Formatter> formatterSupplier)
        throws IOException
    {
        this(localAddress(DEFAULT_PORT), formatterSupplier);
    }

    public UdpSyslog(final int port) throws IOException {
        this(localAddress(port));
    }

    public UdpSyslog(final SocketAddress remote) throws IOException {
        this(remote, Rfc5424FormatterSupplier.INSTANCE);
    }

    public UdpSyslog(final int port,
        final Supplier<Formatter> formatterSupplier)
        throws IOException
    {
        this(localAddress(port), formatterSupplier);
    }

    public UdpSyslog(final IOExceptionHandler ioExceptionHandler)
        throws IOException
    {
        this(DEFAULT_PORT, ioExceptionHandler);
    }

    public UdpSyslog(final int port,
        final IOExceptionHandler ioExceptionHandler)
        throws IOException
    {
        this(localAddress(port), ioExceptionHandler,
            Rfc5424FormatterSupplier.INSTANCE);
    }

    public UdpSyslog(final SocketAddress remote,
        final Supplier<Formatter> formatterSupplier)
        throws IOException
    {
        this(remote, IgnoringIOExceptionHandler.INSTANCE, formatterSupplier);
    }

    public UdpSyslog(
        final SocketAddress remote,
        final IOExceptionHandler ioExceptionHandler,
        final Supplier<Formatter> formatterSupplier)
        throws IOException
    {
        super(ioExceptionHandler, formatterSupplier);
        channel = DatagramChannel
            .open()
            .setOption(StandardSocketOptions.SO_SNDBUF, SEND_BUFFER_SIZE)
            .connect(remote);
    }

    @Override
    protected void publish(final ByteBuffer buf) throws IOException {
        channel.write(buf);
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }

    private static SocketAddress localAddress(final int port)
        throws IOException
    {
        return new InetSocketAddress(InetAddress.getByAddress(IPV4_LOCALHOST),
            port);
    }
}

