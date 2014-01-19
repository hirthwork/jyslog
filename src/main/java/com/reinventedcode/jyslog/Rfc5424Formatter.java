package com.reinventedcode.jyslog;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import java.util.List;

public class Rfc5424Formatter extends AbstractFormatter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormatter.ISO_OFFSET_DATE_TIME
            .withZone(ZoneId.systemDefault());

    @Override
    protected void appendHeader(final Record record) {
        sb.append('1');
        sb.append(' ');
        DATE_TIME_FORMATTER.formatTo(record.timestamp(), sb);
        sb.append(' ');
        sb.append(record.hostname());
        sb.append(' ');
        sb.append(record.appname());
        sb.append(' ');
        sb.append(record.procid());
        sb.append(' ');
        sb.append(record.msgid());
        sb.append(' ');
        List<SDElement> structuredData = record.structuredData();
        if (structuredData == null || structuredData.isEmpty()) {
            sb.append('-');
        } else {
            for (SDElement element: structuredData) {
                sb.append('[');
                sb.append(element.id());
                for (SDParam param: element.params()) {
                    sb.append(' ');
                    sb.append(param.name());
                    sb.append('=');
                    sb.append('"');
                    sb.append(param.value());
                    sb.append('"');
                }
                sb.append(']');
            }
        }
    }

    @Override
    public void appendMessagePrefix() {
        sb.append(' ');
        sb.append('\ufeff'); // BOM
    }
}

