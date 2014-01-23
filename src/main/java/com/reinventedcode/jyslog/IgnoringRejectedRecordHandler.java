package com.reinventedcode.jyslog;

public class IgnoringRejectedRecordHandler implements RejectedRecordHandler {
    public static final IgnoringRejectedRecordHandler INSTANCE =
        new IgnoringRejectedRecordHandler();

    @Override
    public void rejected(final Record record) {
    }
}

