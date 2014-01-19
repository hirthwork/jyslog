package com.reinventedcode.jyslog;

import java.util.function.IntPredicate;

public final class StringUtils {
    private StringUtils() {
    }

    public static final String NIL_VALUE = "-";

    public static String normalize(final String originalValue,
        final IntPredicate predicate, final int maxLength)
    {
        String value = originalValue;
        if (value == null || value.isEmpty()) {
            value = NIL_VALUE;
        } else {
            if (value.length() > maxLength) {
                value = value.substring(0, maxLength);
            }
            if (!value.chars().allMatch(predicate)) {
                final StringBuilder builder =
                    new StringBuilder(value.length());
                value.chars()
                    .forEachOrdered(new CharReplacer(builder, predicate));
                value = builder.toString();
            }
        }
        return value;
    }
}

