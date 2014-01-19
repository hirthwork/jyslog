package com.reinventedcode.jyslog;

public class BasicSyslogger extends AbstractSyslogger {
    private final Syslog syslog;

    public BasicSyslogger(final Syslog syslog) {
        this(BasicSourceInfo.DEFAULT, syslog);
    }

    public BasicSyslogger(final SourceInfo sourceInfo, final Syslog syslog) {
        super(sourceInfo);
        this.syslog = syslog;
    }

    @Override
    public BasicSyslogger withSourceInfo(final SourceInfo sourceInfo) {
        return new BasicSyslogger(sourceInfo, syslog);
    }

    @Override
    public void log(final Severity severity, final String message,
        final Throwable error)
    {
        syslog.publish(new BasicRecord(this, severity, message, error));
    }
}

