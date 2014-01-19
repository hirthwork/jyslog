package com.reinventedcode.jyslog;

import java.io.IOException;

import java.nio.ByteBuffer;

public interface Formatter {
    ByteBuffer format(final Record record) throws IOException;
}

