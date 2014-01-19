package com.reinventedcode.jyslog;

import java.util.List;

public interface Syslogger {
    Syslogger withFacility(Facility facility);
    Syslogger withHostname(String hostname);
    Syslogger withAppname(String appname);
    Syslogger withProcid(String procid);
    Syslogger withMsgid(String msgid);
    Syslogger withStructuredData(SDElement... structuredData);
    Syslogger withStructuredData(List<SDElement> structuredData);
    Syslogger withSourceInfo(SourceInfo sourceInfo);

    void emergency(String message);
    void emergency(String message, Throwable error);
    void alert(String message);
    void alert(String message, Throwable error);
    void critical(String message);
    void critical(String message, Throwable error);
    void error(String message);
    void error(String message, Throwable error);
    void warning(String message);
    void warning(String message, Throwable error);
    void notice(String message);
    void notice(String message, Throwable error);
    void info(String message);
    void info(String message, Throwable error);
    void debug(String message);
    void debug(String message, Throwable error);

    void log(Severity severity, String message);
    void log(Severity severity, String message, Throwable error);
}

