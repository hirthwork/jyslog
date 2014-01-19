package com.reinventedcode.jyslog;

public class SDNamePredicate extends PrintUsAsciiPredicate {
    public static final SDNamePredicate INSTANCE = new SDNamePredicate();

    @Override
    public boolean test(final int value) {
        if (super.test(value)) {
            switch (value) {
                case ' ':
                case '"':
                case '=':
                case ']':
                    break;

                default:
                    return true;
            }
        }
        return false;
    }
}

