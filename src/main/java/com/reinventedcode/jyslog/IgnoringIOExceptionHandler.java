package com.reinventedcode.jyslog;

import java.io.IOException;

public class IgnoringIOExceptionHandler implements IOExceptionHandler {
    public static final IgnoringIOExceptionHandler INSTANCE =
        new IgnoringIOExceptionHandler();

    @Override
    public void handleException(final IOException e) {
    }
}

