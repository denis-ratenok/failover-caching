package com.example.failovercacher.exeption;

public class MissedCacheException extends RuntimeException {
    public MissedCacheException(String message) {
        super(message);
    }
}
