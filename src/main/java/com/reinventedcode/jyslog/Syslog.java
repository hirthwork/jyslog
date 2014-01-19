package com.reinventedcode.jyslog;

import java.io.Closeable;

public interface Syslog extends Closeable {
    void publish(Record record);
}

