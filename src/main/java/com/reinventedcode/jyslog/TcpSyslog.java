package com.reinventedcode.jyslog;

import java.io.IOException;

import java.net.SocketAddress;
import java.net.StandardSocketOptions;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class TcpSyslog extends Thread implements Syslog {
    private static final int SEND_BUFFER_SIZE = 1048576;

    public static final int DEFAULT_PORT = 514;

    private final SocketAddress remote;
    private final Formatter formatter;
    private final IOExceptionHandler ioExceptionHandler;
    private final RejectedRecordHandler rejectedRecordHandler;
    private final BlockingQueue<Record> messageQueue;
    private volatile boolean closed = false;

    public TcpSyslog() throws IOException {
        this(DEFAULT_PORT);
    }

    public TcpSyslog(final int port) throws IOException {
        this(SocketAddressUtils.localAddress(port));
    }

    public TcpSyslog(final SocketAddress remote) {
        this(remote, new TcpSyslogConfig());
    }

    public TcpSyslog(final TcpSyslogConfig config) throws IOException {
        this(DEFAULT_PORT, config);
    }

    public TcpSyslog(final int port, final TcpSyslogConfig config)
        throws IOException
    {
        this(SocketAddressUtils.localAddress(port), config);
    }

    public TcpSyslog(final SocketAddress remote,
        final TcpSyslogConfig config)
    {
        this.remote = Objects.requireNonNull(remote);
        this.formatter = config.formatter();
        this.ioExceptionHandler = config.ioExceptionHandler();
        this.rejectedRecordHandler = config.rejectedRecordHandler();
        this.messageQueue = new ArrayBlockingQueue<>(config.queueSize());
    }

    @Override
    public void publish(final Record record) {
        if (!closed && !messageQueue.offer(record)) {
            rejectedRecordHandler.rejected(record);
        }
    }

    @Override
    public void close() {
        closed = true;
        interrupt();
        try {
            join();
        } catch (InterruptedException e) {
            return;
        }
    }

    @Override
    public void run() {
        SocketChannel channel = null;
        Record record = null;
        while (!closed || record != null) {
            record = messageQueue.poll();
            if (record == null && !closed) {
                try {
                    record = messageQueue.poll(1, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    record = null;
                }
            }
            if (record != null) {
                if (channel == null) {
                    try {
                        channel = SocketChannel.open(remote)
                            .setOption(
                                StandardSocketOptions.SO_SNDBUF,
                                SEND_BUFFER_SIZE)
                            .setOption(
                                StandardSocketOptions.SO_KEEPALIVE,
                                Boolean.TRUE)
                            .setOption(
                                StandardSocketOptions.SO_REUSEADDR,
                                Boolean.TRUE)
                            .setOption(
                                StandardSocketOptions.TCP_NODELAY,
                                Boolean.TRUE)
                            .setOption(StandardSocketOptions.SO_LINGER, 1);
                    } catch (IOException e) {
                        ioExceptionHandler.handleException(e, record);
                        continue;
                    }
                }
                ByteBuffer buf = formatter.format(record);
                try {
                    channel.write(buf);
                } catch (IOException e) {
                    try {
                        channel.close();
                    } catch (IOException ex) {
                        e.addSuppressed(ex);
                    }
                    channel = null;
                    ioExceptionHandler.handleException(e, record);
                }
            }
        }
        if (channel != null) {
            try {
                channel.close();
            } catch (IOException e) {
                return;
            }
        }
    }
}

