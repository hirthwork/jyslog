package com.reinventedcode.jyslog;

import java.util.function.IntConsumer;
import java.util.function.IntPredicate;

public class CharReplacer implements IntConsumer {
    private final StringBuilder sb;
    private final IntPredicate predicate;
    private final char replacement;

    public CharReplacer(final StringBuilder sb, final IntPredicate predicate) {
        this(sb, predicate, '_');
    }

    public CharReplacer(final StringBuilder sb, final IntPredicate predicate,
        final char replacement)
    {
        this.sb = sb;
        this.predicate = predicate;
        this.replacement = replacement;
    }

    @Override
    public void accept(final int value) {
        char c;
        if (predicate.test(value)) {
            c = (char) value;
        } else {
            c = replacement;
        }
        sb.append(c);
    }
}

