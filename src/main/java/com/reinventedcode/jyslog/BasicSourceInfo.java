package com.reinventedcode.jyslog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BasicSourceInfo implements SourceInfo {
    private static final int MAX_HOSTNAME_LENGTH = 255;
    private static final int MAX_APPNAME_LENGTH = 48;
    private static final int MAX_PROCID_LENGTH = 128;
    private static final int MAX_MSGID_LENGTH = 32;

    public static final BasicSourceInfo DEFAULT = new BasicSourceInfo(
        Facility.USER,
        StringUtils.normalize(InetAddressUtils.localFqdn(),
            PrintUsAsciiPredicate.INSTANCE, MAX_HOSTNAME_LENGTH),
        "java",
        StringUtils.NIL_VALUE,
        StringUtils.NIL_VALUE,
        null);

    private final Facility facility;
    private final String hostname;
    private final String appname;
    private final String procid;
    private final String msgid;
    private final List<SDElement> structuredData;

    // CSOFF: ParameterNumber
    protected BasicSourceInfo(
        final Facility facility,
        final String hostname,
        final String appname,
        final String procid,
        final String msgid,
        final List<SDElement> structuredData)
    {
        this.facility = facility;
        this.hostname = hostname;
        this.appname = appname;
        this.procid = procid;
        this.msgid = msgid;
        if (structuredData == null || structuredData.isEmpty()) {
            this.structuredData = null;
        } else {
            this.structuredData =
                Collections.unmodifiableList(new ArrayList<>(structuredData));
        }
    }
    // CSON: ParameterNumber

    @Override
    public Facility facility() {
        return facility;
    }

    @Override
    public String hostname() {
        return hostname;
    }

    @Override
    public String appname() {
        return appname;
    }

    @Override
    public String procid() {
        return procid;
    }

    @Override
    public String msgid() {
        return msgid;
    }

    @Override
    public List<SDElement> structuredData() {
        return structuredData;
    }

    @Override
    public BasicSourceInfo withFacility(final Facility facility) {
        return new BasicSourceInfo(
            facility,
            hostname(),
            appname(),
            procid(),
            msgid(),
            structuredData());
    }

    @Override
    public BasicSourceInfo withHostname(final String hostname) {
        return new BasicSourceInfo(
            facility(),
            StringUtils.normalize(hostname, PrintUsAsciiPredicate.INSTANCE,
                MAX_HOSTNAME_LENGTH),
            appname(),
            procid(),
            msgid(),
            structuredData());
    }

    @Override
    public BasicSourceInfo withAppname(final String appname) {
        return new BasicSourceInfo(
            facility(),
            hostname(),
            StringUtils.normalize(appname, PrintUsAsciiPredicate.INSTANCE,
                MAX_APPNAME_LENGTH),
            procid(),
            msgid(),
            structuredData());
    }

    @Override
    public BasicSourceInfo withProcid(final String procid) {
        return new BasicSourceInfo(
            facility(),
            hostname(),
            appname(),
            StringUtils.normalize(procid, PrintUsAsciiPredicate.INSTANCE,
                MAX_PROCID_LENGTH),
            msgid(),
            structuredData());
    }

    @Override
    public BasicSourceInfo withMsgid(final String msgid) {
        return new BasicSourceInfo(
            facility(),
            hostname(),
            appname(),
            procid(),
            StringUtils.normalize(msgid, PrintUsAsciiPredicate.INSTANCE,
                MAX_MSGID_LENGTH),
            structuredData());
    }

    @Override
    public BasicSourceInfo withStructuredData(
        final SDElement... structuredData)
    {
        return withStructuredData(Arrays.asList(structuredData));
    }

    @Override
    public BasicSourceInfo withStructuredData(
        final List<SDElement> structuredData)
    {
        return new BasicSourceInfo(
            facility(),
            hostname(),
            appname(),
            procid(),
            msgid(),
            structuredData);
    }
}

