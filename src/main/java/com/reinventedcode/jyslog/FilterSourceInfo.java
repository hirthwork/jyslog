package com.reinventedcode.jyslog;

import java.util.List;

public class FilterSourceInfo implements SourceInfo {
    private final SourceInfo sourceInfo;

    public FilterSourceInfo(final SourceInfo sourceInfo) {
        this.sourceInfo = sourceInfo;
    }

    @Override
    public Facility facility() {
        return sourceInfo.facility();
    }

    @Override
    public String hostname() {
        return sourceInfo.hostname();
    }

    @Override
    public String appname() {
        return sourceInfo.appname();
    }

    @Override
    public String procid() {
        return sourceInfo.procid();
    }

    @Override
    public String msgid() {
        return sourceInfo.msgid();
    }

    @Override
    public List<SDElement> structuredData() {
        return sourceInfo.structuredData();
    }

    @Override
    public SourceInfo withFacility(final Facility facility) {
        return sourceInfo.withFacility(facility);
    }

    @Override
    public SourceInfo withHostname(final String hostname) {
        return sourceInfo.withHostname(hostname);
    }

    @Override
    public SourceInfo withAppname(final String appname) {
        return sourceInfo.withAppname(appname);
    }

    @Override
    public SourceInfo withProcid(final String procid) {
        return sourceInfo.withProcid(procid);
    }

    @Override
    public SourceInfo withMsgid(final String msgid) {
        return sourceInfo.withMsgid(msgid);
    }

    @Override
    public SourceInfo withStructuredData(final SDElement... structuredData) {
        return sourceInfo.withStructuredData(structuredData);
    }

    @Override
    public SourceInfo withStructuredData(
        final List<SDElement> structuredData)
    {
        return sourceInfo.withStructuredData(structuredData);
    }
}

