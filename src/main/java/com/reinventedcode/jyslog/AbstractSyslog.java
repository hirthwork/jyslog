package com.reinventedcode.jyslog;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.util.function.Supplier;

public abstract class AbstractSyslog implements Syslog {
    private final IOExceptionHandler ioExceptionHandler;
    private final ThreadLocal<Formatter> formatter;

    public AbstractSyslog(final IOExceptionHandler ioExceptionHandler,
        final Supplier<Formatter> formatterSupplier)
    {
        this.ioExceptionHandler = ioExceptionHandler;
        formatter = ThreadLocal.withInitial(formatterSupplier);
    }

    protected abstract void publish(ByteBuffer buf) throws IOException;

    @Override
    public void publish(final Record record) {
        try {
            publish(formatter.get().format(record));
        } catch (IOException e) {
            ioExceptionHandler.handleException(e);
        }
    }
}

