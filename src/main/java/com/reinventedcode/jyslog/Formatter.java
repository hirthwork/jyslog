package com.reinventedcode.jyslog;

import java.nio.ByteBuffer;

public interface Formatter {
    ByteBuffer format(Record record);
}

