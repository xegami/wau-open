package com.xegami.wau.api.service.exception;

public class WauException extends Exception {

    public WauException(Exceptions e) {
        super(e.getValue());
    }
}
