package com.reinventedcode.jyslog;

import java.time.Instant;

public interface Record extends SourceInfo {
    Instant timestamp();
    Severity severity();
    String message();
    Throwable error();
}

