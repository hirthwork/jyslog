package com.reinventedcode.jyslog;

import java.io.CharArrayWriter;

public class AppendingCharArrayWriter extends CharArrayWriter {
    public AppendingCharArrayWriter() {
        super();
    }

    public AppendingCharArrayWriter(final int capacity) {
        super(capacity);
    }

    public void appendTo(final StringBuilder builder) {
        builder.append(buf, 0, count);
    }
}

