package com.reinventedcode.jyslog;

import java.util.concurrent.atomic.AtomicLong;

public class CountingRejectedRecordHandler implements RejectedRecordHandler {
    private final AtomicLong rejected = new AtomicLong();

    @Override
    public void rejected(final Record record) {
        rejected.incrementAndGet();
    }

    public long rejected() {
        return rejected.get();
    }
}

