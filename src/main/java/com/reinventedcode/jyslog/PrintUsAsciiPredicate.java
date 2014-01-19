package com.reinventedcode.jyslog;

import java.util.function.IntPredicate;

public class PrintUsAsciiPredicate implements IntPredicate {
    public static final PrintUsAsciiPredicate INSTANCE =
        new PrintUsAsciiPredicate();

    @Override
    public boolean test(final int value) {
        return value > ' ' && value <= '~';
    }
}

