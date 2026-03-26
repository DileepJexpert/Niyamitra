package com.niyamitra.common.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String resource, String field, Object value) {
        super(resource + " with " + field + " '" + value + "' already exists");
    }
}
