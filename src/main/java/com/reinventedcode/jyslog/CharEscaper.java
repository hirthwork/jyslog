package com.reinventedcode.jyslog;

import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

public class CharEscaper implements IntConsumer {
    private final StringBuilder sb;
    private final IntPredicate predicate;

    public CharEscaper(final StringBuilder sb, final IntPredicate predicate) {
        this.sb = sb;
        this.predicate = predicate;
    }

    @Override
    public void accept(final int value) {
        if (!predicate.test(value)) {
            sb.append('\\');
        }
        sb.append((char) value);
    }
}

