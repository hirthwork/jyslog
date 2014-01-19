package com.reinventedcode.jyslog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BasicSDElement implements SDElement {
    private final String id;
    private final List<SDParam> params;

    public BasicSDElement(final String id, final SDParam... params) {
        this(id, Arrays.asList(params));
    }

    public BasicSDElement(final String id, final List<SDParam> params) {
        this.id = StringUtils.normalize(id, SDNamePredicate.INSTANCE,
            BasicSDParam.MAX_SD_NAME_LENGTH);
        this.params = new ArrayList<>(params);
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public List<SDParam> params() {
        return params;
    }

    public BasicSDElement param(final String name, final String value) {
        return param(new BasicSDParam(name, value));
    }

    public BasicSDElement param(final SDParam param) {
        params.add(param);
        return this;
    }
}

