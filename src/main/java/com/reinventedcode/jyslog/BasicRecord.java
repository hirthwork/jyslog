package com.reinventedcode.jyslog;

import java.time.Instant;

public class BasicRecord extends FilterSourceInfo implements Record {
    private final Instant timestamp = Instant.now();
    private final Severity severity;
    private final String message;
    private final Throwable error;

    // CSOFF: ParameterNumber
    public BasicRecord(
        final SourceInfo sourceInfo,
        final Severity severity,
        final String message,
        final Throwable error)
    {
        super(sourceInfo);
        this.severity = severity;
        this.message = message;
        this.error = error;
    }
    // CSON: ParameterNumber

    @Override
    public Instant timestamp() {
        return timestamp;
    }

    @Override
    public Severity severity() {
        return severity;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public Throwable error() {
        return error;
    }
}

