package com.reinventedcode.jyslog;

import java.util.function.IntPredicate;

public class SDValuePredicate implements IntPredicate {
    public static final SDValuePredicate INSTANCE = new SDValuePredicate();

    @Override
    public boolean test(final int value) {
        switch (value) {
            case '"':
            case '\\':
            case ']':
                return false;

            default:
                return true;
        }
    }
}

