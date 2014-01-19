package com.reinventedcode.jyslog;

import java.net.InetAddress;
import java.net.UnknownHostException;

public final class InetAddressUtils {
    private InetAddressUtils() {
    }

    public static String localFqdn() {
        try {
            return InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            return null;
        }
    }
}

