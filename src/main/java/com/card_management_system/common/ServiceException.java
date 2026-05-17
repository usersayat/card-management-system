package com.card_management_system.common;

import java.util.List;

public class ServiceException extends RuntimeException {
    private final int errorCode;
    private final List<String> details;

    public ServiceException(String message, Throwable cause, int errorCode, List<String> details) {
        super(message, cause);
        this.errorCode = errorCode;
        this.details = details;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public List<String> getDetails() {
        return details;
    }
}
