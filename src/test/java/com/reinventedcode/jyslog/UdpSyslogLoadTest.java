package com.reinventedcode.jyslog;

import java.io.Closeable;
import java.io.IOException;

import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;

public class UdpSyslogLoadTest {
    private static final int SILENCE_INTERVAL = 100;
    private static final int CONCURRENCY = 10;
    private static final int MAX_LENGTH = 8192;
    private static final int MESSAGES = 32768;
    private static final int BUFFER_SIZE = MESSAGES << 1;
    private static final int RCV_BUFFER_SIZE = 10485760;

    private static class Server extends Thread implements Closeable {
        private final AtomicInteger receivedMessages = new AtomicInteger();
        private final AtomicInteger receivedBytes = new AtomicInteger();
        private final ByteBuffer buffer =
            ByteBuffer.allocateDirect(BUFFER_SIZE);
        private final DatagramChannel channel;
        private final int port;
        private volatile Exception exception = null;
        private volatile long lastProcessed = System.currentTimeMillis();

        public Server() throws IOException {
            channel = DatagramChannel
                .open()
                .setOption(StandardSocketOptions.SO_RCVBUF, RCV_BUFFER_SIZE)
                .bind(null);
            port = ((InetSocketAddress) channel.getLocalAddress()).getPort();
        }

        public int port() {
            return port;
        }

        public int receivedMessages() {
            return receivedMessages.get();
        }

        public int receivedBytes() {
            return receivedBytes.get();
        }

        public Exception exception() {
            return exception;
        }

        @Override
        public void close() throws IOException {
            try {
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
            while (true) {
                try {
                    channel.receive(buffer);
                    receivedMessages.incrementAndGet();
                    receivedBytes.addAndGet(buffer.position());
                    buffer.position(0);
                    lastProcessed = System.currentTimeMillis();
                } catch (ClosedChannelException e) {
                    break;
                } catch (Exception e) {
                    exception = e;
                    break;
                }
            }
        }
    }

    private static class Shooter implements Callable<Object> {
        private final Syslogger syslogger;
        private final String message;

        public Shooter(final Syslogger syslogger, final String message) {
            this.syslogger = syslogger;
            this.message = message;
        }

        @Override
        public Object call() {
            syslogger.info(message);
            return null;
        }
    }

    @Test
    public void testLoad() throws Exception {
        String[] messages = new String[MESSAGES];
        messages[0] = "Hello, world";
        char c = 'a';
        for (int i = 1; i < MESSAGES; ++i) {
            messages[i] = messages[i - 1] + c;
            if (messages[i].length() > MAX_LENGTH) {
                ++c;
                messages[i] = messages[0] + c;
            }
        }
        Collections.shuffle(Arrays.asList(messages));
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
            CONCURRENCY,
            CONCURRENCY,
            1,
            TimeUnit.DAYS,
            new ArrayBlockingQueue<Runnable>(MESSAGES));

        Server server = new Server();
        server.start();
        BasicIOExceptionHandler handler = new BasicIOExceptionHandler();
        AtomicInteger formattedBytes = new AtomicInteger();
        Supplier<Formatter> countingFormatterSupplier =
            new Supplier<Formatter>() {
                @Override
                public Formatter get() {
                    return new Rfc5424Formatter() {
                        @Override
                        public ByteBuffer format(final Record record) {
                            ByteBuffer buf = super.format(record);
                            formattedBytes.addAndGet(buf.remaining());
                            return buf;
                        }
                    };
                }
            };
        try (Closeable guard = server;
            Syslog syslog = new UdpSyslog(server.port(), handler,
                countingFormatterSupplier))
        {
            Syslogger logger =
                new BasicSyslogger(BasicSourceInfo.DEFAULT, syslog);
            executor.prestartAllCoreThreads();
            List<Callable<Object>> tasks = new ArrayList<>();
            for (String message: messages) {
                tasks.add(new Shooter(logger, message));
            }
            executor.invokeAll(tasks);
        } finally {
            executor.shutdown();
        }
        Assert.assertNull(server.exception());
        Assert.assertNull(handler.exception());
        Assert.assertEquals(MESSAGES, server.receivedMessages());
        Assert.assertEquals(formattedBytes.get(), server.receivedBytes());
    }
}

