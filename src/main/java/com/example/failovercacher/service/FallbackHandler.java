package com.example.failovercacher.service;

public interface FallbackHandler<T> {
    T getData();
}
