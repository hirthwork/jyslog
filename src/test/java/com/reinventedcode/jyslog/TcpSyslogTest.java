package com.reinventedcode.jyslog;

import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class TcpSyslogTest {
    private static final int SLEEP_REPEATS = 10;
    private static final int SLEEP_INTERVAL = 100;
    private static final int REPEATS = 1000;
    private static final int MAX_SIZE = 1024;
    private static final int BUFFER_SIZE = 2048;

    @Test
    public void testRfc5424() throws Exception {
        try (ServerSocketChannel serverChannel =
                ServerSocketChannel.open().bind(null))
        {
            InetSocketAddress addr =
                (InetSocketAddress) serverChannel.getLocalAddress();
            try (TcpSyslog syslog = new TcpSyslog(addr.getPort())) {
                syslog.start();
                Syslogger logger = new BasicSyslogger(syslog)
                    .withAppname("tcptest")
                    .withFacility(Facility.LPR);
                ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
                String messageText = "Testing rfc5424";
                SocketChannel channel = null;
                for (int i = 0; i < REPEATS; ++i) {
                    logger.warning(messageText);
                    if (channel == null) {
                        channel = serverChannel.accept();
                    }
                    buf.clear();
                    channel.read(buf);
                    String message = new String(buf.array(), 0, buf.position(),
                        StandardCharsets.UTF_8);
                    int firstSpace = message.indexOf(' ');
                    int secondSpace = message.indexOf(' ', firstSpace + 1);
                    int thirdSpace = message.indexOf(' ', secondSpace + 1);
                    int size = Integer.parseInt(
                        new String(buf.array(), 0, firstSpace));
                    Assert.assertEquals(size + firstSpace + 1, buf.position());
                    message = message.substring(firstSpace + 1, secondSpace)
                        + message.substring(thirdSpace);
                    Assert.assertEquals(
                        "<52>1 " + BasicSourceInfo.DEFAULT.hostname()
                        + " tcptest - - - \ufeff" + messageText,
                        message);
                    messageText = messageText + '1';
                }
                channel.close();
            }
        }
    }

    @Test
    public void testClosedDestination() throws Exception {
        int port;
        try (ServerSocketChannel serverChannel =
                ServerSocketChannel.open().bind(null))
        {
            port = ((InetSocketAddress) serverChannel.getLocalAddress())
                .getPort();
        }
        CountingIOExceptionHandler exceptionHandler =
            new CountingIOExceptionHandler();
        CountingRejectedRecordHandler rejectHandler =
            new CountingRejectedRecordHandler();
        try (TcpSyslog syslog = new TcpSyslog(port,
                new TcpSyslogConfig()
                    .ioExceptionHandler(exceptionHandler)
                    .rejectedRecordHandler(rejectHandler)))
        {
            syslog.start();
            Syslogger logger = new BasicSyslogger(syslog);
            for (int i = 0; i < REPEATS << 1; ++i) {
                logger.info("You shall not pass!");
            }
            for (int i = 0; i < SLEEP_REPEATS; ++i) {
                if (rejectHandler.rejected() + exceptionHandler.failed()
                    < REPEATS << 1)
                {
                    Thread.sleep(SLEEP_INTERVAL);
                } else {
                    break;
                }
            }
            Assert.assertEquals(REPEATS << 1,
                rejectHandler.rejected() + exceptionHandler.failed());
            try (ServerSocketChannel serverChannel = ServerSocketChannel.open()
                    .bind(new InetSocketAddress(port)))
            {
                logger.error("Come back to the light");
                ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
                try (SocketChannel channel = serverChannel.accept()) {
                    channel.read(buf);
                }
                String message = new String(buf.array(), 0, buf.position(),
                    StandardCharsets.UTF_8);
                int firstSpace = message.indexOf(' ');
                int secondSpace = message.indexOf(' ', firstSpace + 1);
                int thirdSpace = message.indexOf(' ', secondSpace + 1);
                int size = Integer.parseInt(
                    new String(buf.array(), 0, firstSpace));
                Assert.assertEquals(size + firstSpace + 1, buf.position());
                message = message.substring(firstSpace + 1, secondSpace)
                    + message.substring(thirdSpace);
                Assert.assertEquals(
                    "<11>1 " + BasicSourceInfo.DEFAULT.hostname()
                    + " java - - - \ufeffCome back to the light",
                    message);
            }
        }
    }
}

