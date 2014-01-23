package com.reinventedcode.jyslog;

import java.util.function.Supplier;

public class Rfc5424FormatterSupplier implements Supplier<Rfc5424Formatter> {
    public static final Rfc5424FormatterSupplier INSTANCE =
        new Rfc5424FormatterSupplier();

    @Override
    public Rfc5424Formatter get() {
        return new Rfc5424Formatter();
    }
}

