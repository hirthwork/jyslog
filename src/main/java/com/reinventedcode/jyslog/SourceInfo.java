package com.reinventedcode.jyslog;

import java.util.List;

public interface SourceInfo {
    Facility facility();
    String hostname();
    String appname();
    String procid();
    String msgid();
    List<SDElement> structuredData();

    SourceInfo withFacility(Facility facility);
    SourceInfo withHostname(String hostname);
    SourceInfo withAppname(String appname);
    SourceInfo withProcid(String procid);
    SourceInfo withMsgid(String msgid);
    SourceInfo withStructuredData(SDElement... structuredData);
    SourceInfo withStructuredData(List<SDElement> structuredData);
}

