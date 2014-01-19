package com.reinventedcode.jyslog;

import java.io.IOException;

import java.nio.ByteBuffer;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;

import java.util.Locale;

public class Rfc3164Formatter extends AbstractFormatter {
    private static final int MAX_SIZE = 1024;
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        new DateTimeFormatterBuilder()
            .appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT)
            .appendLiteral(' ')
            .padNext(2)
            .appendValue(ChronoField.DAY_OF_MONTH)
            .appendLiteral(' ')
            .appendValue(ChronoField.HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
            .appendLiteral(':')
            .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
            .toFormatter(Locale.ENGLISH)
            .withZone(ZoneId.systemDefault());

    @Override
    protected void appendHeader(final Record record) {
        DATE_TIME_FORMATTER.formatTo(record.timestamp(), sb);
        sb.append(' ');
        sb.append(record.hostname());
        sb.append(' ');
        sb.append(record.appname());
    }

    @Override
    protected void appendMessagePrefix() {
        sb.append(':');
        sb.append(' ');
    }

    @Override
    public ByteBuffer format(final Record record) throws IOException {
        ByteBuffer buf = super.format(record);
        int position = buf.position();
        if (buf.limit() - position > MAX_SIZE) {
            buf.limit(position + MAX_SIZE);
        }
        return buf;
    }
}

