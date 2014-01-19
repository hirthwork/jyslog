package com.reinventedcode.jyslog;

import java.util.List;

public abstract class AbstractSyslogger
    extends FilterSourceInfo
    implements Syslogger
{
    public AbstractSyslogger(final SourceInfo sourceInfo) {
        super(sourceInfo);
    }

    @Override
    public abstract AbstractSyslogger withSourceInfo(SourceInfo sourceInfo);

    @Override
    public AbstractSyslogger withFacility(final Facility facility) {
        return withSourceInfo(super.withFacility(facility));
    }

    @Override
    public AbstractSyslogger withHostname(final String hostname) {
        return withSourceInfo(super.withHostname(hostname));
    }

    @Override
    public AbstractSyslogger withAppname(final String appname) {
        return withSourceInfo(super.withAppname(appname));
    }

    @Override
    public AbstractSyslogger withProcid(final String procid) {
        return withSourceInfo(super.withProcid(procid));
    }

    @Override
    public AbstractSyslogger withMsgid(final String msgid) {
        return withSourceInfo(super.withMsgid(msgid));
    }

    @Override
    public AbstractSyslogger withStructuredData(
        final SDElement... structuredData)
    {
        return withSourceInfo(super.withStructuredData(structuredData));
    }

    @Override
    public AbstractSyslogger withStructuredData(
        final List<SDElement> structuredData)
    {
        return withSourceInfo(super.withStructuredData(structuredData));
    }

    // TODO: Make a default method in Syslogger, once Checkstyle will stop
    // crashing on Java 8 syntax
    @Override
    public void emergency(final String message) {
        emergency(message, null);
    }

    @Override
    public void emergency(final String message, final Throwable error) {
        log(Severity.EMERGENCY, message, error);
    }

    @Override
    public void alert(final String message) {
        alert(message, null);
    }

    @Override
    public void alert(final String message, final Throwable error) {
        log(Severity.ALERT, message, error);
    }

    @Override
    public void critical(final String message) {
        critical(message, null);
    }

    @Override
    public void critical(final String message, final Throwable error) {
        log(Severity.CRITICAL, message, error);
    }

    @Override
    public void error(final String message) {
        error(message, null);
    }

    @Override
    public void error(final String message, final Throwable error) {
        log(Severity.ERROR, message, error);
    }

    @Override
    public void warning(final String message) {
        warning(message, null);
    }

    @Override
    public void warning(final String message, final Throwable error) {
        log(Severity.WARNING, message, error);
    }

    @Override
    public void notice(final String message) {
        notice(message, null);
    }

    @Override
    public void notice(final String message, final Throwable error) {
        log(Severity.NOTICE, message, error);
    }

    @Override
    public void info(final String message) {
        info(message, null);
    }

    @Override
    public void info(final String message, final Throwable error) {
        log(Severity.INFO, message, error);
    }

    @Override
    public void debug(final String message) {
        debug(message, null);
    }

    @Override
    public void debug(final String message, final Throwable error) {
        log(Severity.DEBUG, message, error);
    }

    @Override
    public void log(final Severity severity, final String message) {
        log(severity, message, null);
    }
}

