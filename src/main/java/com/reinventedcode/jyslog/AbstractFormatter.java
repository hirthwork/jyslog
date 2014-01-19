package com.reinventedcode.jyslog;

import java.io.IOException;
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
    private static final int INITIAL_BSIZE = bytesRequired(INITIAL_SIZE);
    private static final int MILLIS_DIGITS = 3;
    private static final int FACILITY_SHIFT = 3;

    protected final StringBuilder sb = new StringBuilder(INITIAL_SIZE);
    private final CharsetEncoder encoder = StandardCharsets.UTF_8.newEncoder()
        .onMalformedInput(CodingErrorAction.REPLACE)
        .onUnmappableCharacter(CodingErrorAction.REPLACE);
    private final AppendingCharArrayWriter appendingWriter =
        new AppendingCharArrayWriter(INITIAL_SIZE);
    private ByteBuffer bb = ByteBuffer.allocateDirect(INITIAL_BSIZE);
    private byte[] buf = new byte[INITIAL_BSIZE];
    private char[] cbuf = new char[INITIAL_SIZE];

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

    @Override
    public ByteBuffer format(final Record record) throws IOException {
        appendPriority(record);
        appendHeader(record);
        appendMessage(record);

        // it's time to convert chars to bytes
        int len = sb.length();
        if (cbuf.length < len) {
            cbuf = new char[Math.max(cbuf.length << 1, len)];
        }
        sb.getChars(0, len, cbuf, 0);
        int blen = bytesRequired(len);
        if (buf.length < blen) {
            buf = new byte[Math.max(buf.length << 1, blen)];
        }
        CharBuffer cb = CharBuffer.wrap(cbuf, 0, len);
        ByteBuffer bb = ByteBuffer.wrap(buf);
        encoder.encode(cb, bb, true);
        blen = bb.position();
        int capacity = this.bb.capacity();
        if (capacity < blen) {
            this.bb = ByteBuffer.allocateDirect(Math.max(capacity << 1, blen));
        } else {
            this.bb.clear();
        }
        this.bb.put(buf, 0, blen);
        this.bb.flip();
        return this.bb;
    }

    private static int bytesRequired(final int chars) {
        return (int) (chars * MAX_BYTES_PER_CHAR);
    }
}

