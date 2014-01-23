package com.reinventedcode.jyslog;

import java.io.IOException;

import java.util.concurrent.atomic.AtomicLong;

public class CountingIOExceptionHandler implements IOExceptionHandler {
    private final AtomicLong failed = new AtomicLong();

    @Override
    public void handleException(final IOException e, final Record record) {
        failed.incrementAndGet();
    }

    public long failed() {
        return failed.get();
    }
}

