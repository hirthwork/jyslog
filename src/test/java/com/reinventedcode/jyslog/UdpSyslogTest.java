package com.reinventedcode.jyslog;

import java.net.InetSocketAddress;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;

public class UdpSyslogTest {
    private static final int REPEATS = 1000;
    private static final int MAX_SIZE = 1024;
    private static final int BUFFER_SIZE = 2048;

    @Test
    public void testRfc5424() throws Exception {
        try (DatagramChannel channel = DatagramChannel.open().bind(null)) {
            InetSocketAddress addr =
                (InetSocketAddress) channel.getLocalAddress();
            try (Syslog syslog = new UdpSyslog(addr.getPort())) {
                Syslogger logger =
                    new BasicSyslogger(BasicSourceInfo.DEFAULT, syslog);
                ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
                String messageText = "Testing rfc5424";
                for (int i = 0; i < REPEATS; ++i) {
                    logger.warning(messageText);
                    buf.clear();
                    Assert.assertNotNull(channel.receive(buf));
                    String message = new String(buf.array(), 0, buf.position(),
                        StandardCharsets.UTF_8);
                    int firstSpace = message.indexOf(' ');
                    int secondSpace = message.indexOf(' ', firstSpace + 1);
                    message = message.substring(0, firstSpace)
                        + message.substring(secondSpace);
                    Assert.assertEquals(
                        "<12>1 " + BasicSourceInfo.DEFAULT.hostname()
                        + " java - - - \ufeff" + messageText,
                        message);
                    messageText = messageText + '1';
                }
            }
        }
    }

    @Test
    public void testStructuredData() throws Exception {
        try (DatagramChannel channel = DatagramChannel.open().bind(null)) {
            InetSocketAddress addr =
                (InetSocketAddress) channel.getLocalAddress();
            try (Syslog syslog = new UdpSyslog(addr.getPort())) {
                Syslogger logger =
                    new BasicSyslogger(BasicSourceInfo.DEFAULT, syslog)
                        .withFacility(Facility.LOCAL7)
                        .withProcid("bad proc id")
                        .withMsgid("0123456789abcdef0123456789abcdef cut")
                        .withStructuredData(
                            new BasicSDElement("elemid")
                                .param("name", "value")
                                .param("bad]name", "bad value]"),
                            new BasicSDElement("bad=id"));
                String messageText = "Testing structured data";
                logger.debug(messageText);
                ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
                Assert.assertNotNull(channel.receive(buf));
                String message = new String(buf.array(), 0, buf.position(),
                    StandardCharsets.UTF_8);
                int firstSpace = message.indexOf(' ');
                int secondSpace = message.indexOf(' ', firstSpace + 1);
                message = message.substring(0, firstSpace)
                    + message.substring(secondSpace);
                Assert.assertEquals(
                    "<191>1 " + BasicSourceInfo.DEFAULT.hostname()
                    + " java bad_proc_id 0123456789abcdef0123456789abcdef "
                    + "[elemid name=\"value\" bad_name=\"bad value\\]\"]"
                    + "[bad_id] \ufeff" + messageText,
                    message);
            }
        }
    }

    @Test
    public void testRfc3164() throws Exception {
        try (DatagramChannel channel = DatagramChannel.open().bind(null)) {
            InetSocketAddress addr =
                (InetSocketAddress) channel.getLocalAddress();
            try (Syslog syslog = new UdpSyslog(addr.getPort(),
                    Rfc3164FormatterSupplier.INSTANCE))
            {
                Syslogger logger =
                    new BasicSyslogger(BasicSourceInfo.DEFAULT, syslog)
                        .withHostname("example.com")
                        .withAppname("tester");
                String messageText = "Testing rfc3164";
                ByteBuffer buf = ByteBuffer.allocate(BUFFER_SIZE);
                boolean uncut = false;
                boolean truncated = false;
                for (int i = 0; i < REPEATS; ++i) {
                    logger.alert(messageText);
                    buf.clear();
                    Assert.assertNotNull(channel.receive(buf));
                    Assert.assertTrue(buf.position() <= MAX_SIZE);
                    String message = new String(buf.array(), 0, buf.position(),
                        StandardCharsets.UTF_8);
                    final int timestampLength = 15;
                    int priEnd = message.indexOf('>') + 1;
                    message = message.substring(0, priEnd)
                        + message.substring(priEnd + timestampLength);
                    String expected = "<9> example.com tester: " + messageText;
                    if (buf.position() < MAX_SIZE) {
                        uncut = true;
                        Assert.assertEquals(expected, message);
                    } else {
                        truncated = true;
                        Assert.assertEquals(
                            expected.substring(0, MAX_SIZE - timestampLength),
                            message);
                    }
                    messageText = messageText + '2';
                }
                Assert.assertTrue(uncut);
                Assert.assertTrue(truncated);
            }
        }
    }
}

