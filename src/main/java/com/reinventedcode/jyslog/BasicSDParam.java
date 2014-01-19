package com.reinventedcode.jyslog;

public class BasicSDParam implements SDParam {
    public static final int MAX_SD_NAME_LENGTH = 32;

    private final String name;
    private final String value;

    public BasicSDParam(final String name, final String value) {
        this.name = StringUtils.normalize(name, SDNamePredicate.INSTANCE,
            MAX_SD_NAME_LENGTH);
        if (value == null) {
            this.value = "";
        } else {
            if (value.chars().allMatch(SDValuePredicate.INSTANCE)) {
                this.value = value;
            } else {
                StringBuilder sb = new StringBuilder(value.length() << 1);
                value.chars().forEachOrdered(
                    new CharEscaper(sb, SDValuePredicate.INSTANCE));
                this.value = sb.toString();
            }
        }
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String value() {
        return value;
    }
}

