package com.reinventedcode.jyslog;

import java.io.IOException;

public class BasicIOExceptionHandler implements IOExceptionHandler {
    private IOException exception = null;

    @Override
    public synchronized void handleException(final IOException exception,
        final Record record)
    {
        if (this.exception == null) {
            this.exception = exception;
        } else {
            this.exception.addSuppressed(exception);
        }
    }

    public synchronized IOException exception() {
        IOException exception = this.exception;
        this.exception = null;
        return exception;
    }
}

