package com.reinventedcode.jyslog;

import java.io.Closeable;
import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;

public class TcpSyslogLoadTest {
    private static final int SILENCE_INTERVAL = 100;
    private static final int CONCURRENCY = 10;
    private static final int MAX_LENGTH = 8192;
    private static final int MESSAGES = 32768;
    private static final int RCV_BUFFER_SIZE = 10485760;
    private static final int BUFFER_SIZE = MESSAGES << 1;

    private static class Server extends Thread implements Closeable {
        private final AtomicInteger receivedBytes = new AtomicInteger();
        private final ByteBuffer buffer =
            ByteBuffer.allocateDirect(BUFFER_SIZE);
        private final ServerSocketChannel serverChannel;
        private final int port;
        private volatile SocketChannel channel = null;
        private volatile Exception exception = null;
        private volatile long lastProcessed = System.currentTimeMillis();

        public Server() throws IOException {
            serverChannel = ServerSocketChannel
                .open()
                .setOption(StandardSocketOptions.SO_RCVBUF, RCV_BUFFER_SIZE)
                .bind(null);
            port = ((InetSocketAddress) serverChannel.getLocalAddress())
                .getPort();
        }

        public Exception exception() {
            return exception;
        }

        public int receivedBytes() {
            return receivedBytes.get();
        }

        public int port() {
            return port;
        }

        @Override
        public void close() throws IOException {
            try {
                serverChannel.close();
                while (System.currentTimeMillis() - lastProcessed
                    < SILENCE_INTERVAL)
                {
                    sleep(SILENCE_INTERVAL);
                }
                channel.close();

                join();
            } catch (InterruptedException e) {
                throw new IOException(e);
            }
        }

        @Override
        public void run() {
            try {
                channel = serverChannel.accept();
                while (true) {
                    try {
                        channel.read(buffer);
                        if (buffer.position() > 0) {
                            receivedBytes.addAndGet(buffer.position());
                            buffer.position(0);
                            lastProcessed = System.currentTimeMillis();
                        }
                    } catch (ClosedChannelException e) {
                        break;
                    }
                }
                channel.close();
            } catch (Exception e) {
                exception = e;
            }
        }
    }

    private static class Shooter implements Callable<Object> {
        private final String message;
        private final Syslogger syslogger;

        public Shooter(final Syslogger syslogger, final String message) {
            this.message = message;
            this.syslogger = syslogger;
        }

        @Override
        public Object call() {
            syslogger.warning(message);
            return null;
        }
    }

    @Test
    public void testLoad() throws Exception {
        String[] messages = new String[MESSAGES];
        messages[0] = "How are you doing?";
        char c = 'a';
        for (int i = 1; i < MESSAGES; ++i) {
            messages[i] = messages[i - 1] + c;
            if (messages[i].length() > MAX_LENGTH) {
                ++c;
                messages[i] = messages[0] + c;
            }
        }

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            CONCURRENCY,
            CONCURRENCY,
            1,
            TimeUnit.DAYS,
            new ArrayBlockingQueue<Runnable>(MESSAGES));

        Collections.shuffle(Arrays.asList(messages));

        Server server = new Server();
        server.start();
        AtomicInteger formattedBytes = new AtomicInteger();
        AtomicInteger formattedMessages = new AtomicInteger();
        CountingIOExceptionHandler exceptionHandler =
            new CountingIOExceptionHandler();
        CountingRejectedRecordHandler rejectHandler =
            new CountingRejectedRecordHandler();
        try (TcpSyslog syslog = new TcpSyslog(server.port(),
                new TcpSyslogConfig()
                    .queueSize(MESSAGES)
                    .ioExceptionHandler(exceptionHandler)
                    .rejectedRecordHandler(rejectHandler)
                    .formatter(new Rfc5424Formatter(true) {
                        @Override
                        public ByteBuffer format(final Record record) {
                            ByteBuffer buf = super.format(record);
                            formattedMessages.incrementAndGet();
                            formattedBytes.addAndGet(buf.remaining());
                            return buf;
                        }
                    }));
            Closeable guard = server)
        {
            syslog.start();
            executor.prestartAllCoreThreads();
            Syslogger logger =
                new BasicSyslogger(BasicSourceInfo.DEFAULT, syslog);
            List<Callable<Object>> tasks = new ArrayList<>();
            for (String message: messages) {
                tasks.add(new Shooter(logger, message));
            }
            executor.invokeAll(tasks);
        } finally {
            executor.shutdown();
        }
        Assert.assertNull(server.exception());
        Assert.assertEquals(0, exceptionHandler.failed());
        Assert.assertEquals(0, rejectHandler.rejected());
        Assert.assertEquals(MESSAGES, formattedMessages.get());
        Assert.assertEquals(formattedBytes.get(), server.receivedBytes());
    }
}

