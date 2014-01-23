package com.reinventedcode.jyslog;

import java.io.IOException;

public interface IOExceptionHandler {
    void handleException(IOException exception, Record record);
}

