package com.reinventedcode.jyslog;

import java.util.Objects;

public class TcpSyslogConfig {
    public static final int DEFAULT_QUEUE_SIZE = 1000;

    private Formatter formatter = new Rfc5424Formatter(true);
    private IOExceptionHandler ioExceptionHandler =
        IgnoringIOExceptionHandler.INSTANCE;
    private RejectedRecordHandler rejectedRecordHandler =
        IgnoringRejectedRecordHandler.INSTANCE;
    private int queueSize = DEFAULT_QUEUE_SIZE;

    public Formatter formatter() {
        return formatter;
    }

    public TcpSyslogConfig formatter(final Formatter formatter) {
        this.formatter = Objects.requireNonNull(formatter);
        return this;
    }

    public IOExceptionHandler ioExceptionHandler() {
        return ioExceptionHandler;
    }

    public TcpSyslogConfig ioExceptionHandler(
        final IOExceptionHandler ioExceptionHandler)
    {
        this.ioExceptionHandler = Objects.requireNonNull(ioExceptionHandler);
        return this;
    }

    public RejectedRecordHandler rejectedRecordHandler() {
        return rejectedRecordHandler;
    }

    public TcpSyslogConfig rejectedRecordHandler(
        final RejectedRecordHandler rejectedRecordHandler)
    {
        this.rejectedRecordHandler =
            Objects.requireNonNull(rejectedRecordHandler);
        return this;
    }

    public int queueSize() {
        return queueSize;
    }

    public TcpSyslogConfig queueSize(final int queueSize) {
        if (queueSize <= 0) {
            throw new IllegalArgumentException();
        }
        this.queueSize = queueSize;
        return this;
    }
}

