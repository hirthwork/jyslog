package com.reinventedcode.jyslog;

import java.io.PrintWriter;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;

public abstract class AbstractFormatter implements Formatter {
    private static final int INITIAL_SIZE = 1024;
    private static final float MAX_BYTES_PER_CHAR =
        StandardCharsets.UTF_8.newEncoder().maxBytesPerChar();
    private static final int FACILITY_SHIFT = 3;
    private static final int OCTETS_COUNT_LENGTH = 11;
    private static final int DECIMAL_DIVISOR = 10;

    protected final StringBuilder sb = new StringBuilder(INITIAL_SIZE);
    private final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE);
    private final AppendingCharArrayWriter appendingWriter =
        new AppendingCharArrayWriter(INITIAL_SIZE);
    private final boolean countOctets;
    private ByteBuffer bb = ByteBuffer.allocateDirect(INITIAL_SIZE);
    private byte[] buf = new byte[INITIAL_SIZE];
    private char[] cbuf = new char[INITIAL_SIZE];

    public AbstractFormatter(final boolean countOctets) {
        this.countOctets = countOctets;
    }

    protected void appendPriority(final Record record) {
        sb.setLength(0);
        sb.append('<');
        sb.append((record.facility().ordinal() << FACILITY_SHIFT)
            | record.severity().ordinal());
        sb.append('>');
    }

    protected abstract void appendHeader(Record record);

    protected abstract void appendMessagePrefix();

    protected void appendMessage(final Record record) {
        String message = record.message();
        Throwable error = record.error();
        if (message != null || error != null) {
            appendMessagePrefix();
            if (message != null) {
                sb.append(message);
                if (error != null) {
                    sb.append(':');
                    sb.append(' ');
                }
            }
            if (error != null) {
                appendingWriter.reset();
                error.printStackTrace(new PrintWriter(appendingWriter));
                appendingWriter.appendTo(sb);
            }
        }
    }

    protected ByteBuffer toByteBuffer(final byte[] buf, final int off,
        final int len)
    {
        int capacity = bb.capacity();
        if (capacity < len) {
            bb = ByteBuffer.allocateDirect(Math.max(capacity << 1, len));
        } else {
            bb.clear();
        }
        bb.put(buf, off, len);
        bb.flip();
        return bb;
    }

    protected ByteBuffer encode() {
        // it's time to convert chars to bytes
        int len = sb.length();
        if (cbuf.length < len) {
            cbuf = new char[Math.max(cbuf.length << 1, len)];
        }
        sb.getChars(0, len, cbuf, 0);
        int blen = bytesRequired(len);
        int off;
        if (countOctets) {
            off = OCTETS_COUNT_LENGTH;
            blen += OCTETS_COUNT_LENGTH;
        } else {
            off = 0;
        }
        if (buf.length < blen) {
            buf = new byte[Math.max(buf.length << 1, blen)];
        }
        CharBuffer cb = CharBuffer.wrap(cbuf, 0, len);
        ByteBuffer bb = ByteBuffer.wrap(buf, off, blen - off);
        encoder.encode(cb, bb, true);
        blen = bb.position() - off;
        if (countOctets) {
            int value = blen;
            buf[--off] = ' ';
            ++blen;
            while (value != 0) {
                int rem = value % DECIMAL_DIVISOR;
                buf[--off] = (byte) ('0' + rem);
                value /= DECIMAL_DIVISOR;
                ++blen;
            }
        }
        return toByteBuffer(buf, off, blen);
    }

    @Override
    public ByteBuffer format(final Record record) {
        appendPriority(record);
        appendHeader(record);
        appendMessage(record);
        return encode();
    }

    private static int bytesRequired(final int chars) {
        return (int) (chars * MAX_BYTES_PER_CHAR);
    }
}

