package com.reinventedcode.jyslog;

import java.io.IOException;

import java.net.SocketAddress;
import java.net.StandardSocketOptions;

import java.nio.channels.DatagramChannel;

import java.util.Objects;
import java.util.function.Supplier;

public class UdpSyslog implements Syslog {
    private static final int SEND_BUFFER_SIZE = 1048576;

    public static final int DEFAULT_PORT = 514;

    private final IOExceptionHandler ioExceptionHandler;
    private final ThreadLocal<Formatter> formatter;
    private final DatagramChannel channel;

    public UdpSyslog() throws IOException {
        this(Rfc5424FormatterSupplier.INSTANCE);
    }

    public UdpSyslog(final Supplier<? extends Formatter> formatterSupplier)
        throws IOException
    {
        this(DEFAULT_PORT, formatterSupplier);
    }

    public UdpSyslog(final int port) throws IOException {
        this(SocketAddressUtils.localAddress(port));
    }

    public UdpSyslog(final SocketAddress remote) throws IOException {
        this(remote, Rfc5424FormatterSupplier.INSTANCE);
    }

    public UdpSyslog(final int port,
        final Supplier<? extends Formatter> formatterSupplier)
        throws IOException
    {
        this(SocketAddressUtils.localAddress(port), formatterSupplier);
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
        this(SocketAddressUtils.localAddress(port), ioExceptionHandler,
            Rfc5424FormatterSupplier.INSTANCE);
    }

    public UdpSyslog(final SocketAddress remote,
        final Supplier<? extends Formatter> formatterSupplier)
        throws IOException
    {
        this(remote, IgnoringIOExceptionHandler.INSTANCE, formatterSupplier);
    }

    public UdpSyslog(
        final int port,
        final IOExceptionHandler ioExceptionHandler,
        final Supplier<? extends Formatter> formatterSupplier)
        throws IOException
    {
        this(SocketAddressUtils.localAddress(port), ioExceptionHandler,
            formatterSupplier);
    }

    public UdpSyslog(
        final SocketAddress remote,
        final IOExceptionHandler ioExceptionHandler,
        final Supplier<? extends Formatter> formatterSupplier)
        throws IOException
    {
        this.ioExceptionHandler = Objects.requireNonNull(ioExceptionHandler);
        formatter = ThreadLocal.withInitial(formatterSupplier);
        channel = DatagramChannel
            .open()
            .setOption(StandardSocketOptions.SO_SNDBUF, SEND_BUFFER_SIZE)
            .setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE)
            .connect(remote);
    }

    @Override
    public void publish(final Record record) {
        try {
            channel.write(formatter.get().format(record));
        } catch (IOException e) {
            ioExceptionHandler.handleException(e, record);
        }
    }

    @Override
    public void close() throws IOException {
        channel.close();
    }
}

