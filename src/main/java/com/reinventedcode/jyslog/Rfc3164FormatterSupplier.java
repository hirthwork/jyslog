package com.reinventedcode.jyslog;

import java.util.function.Supplier;

public class Rfc3164FormatterSupplier implements Supplier<Formatter> {
    public static final Rfc3164FormatterSupplier INSTANCE =
        new Rfc3164FormatterSupplier();

    @Override
    public Rfc3164Formatter get() {
        return new Rfc3164Formatter();
    }
}

