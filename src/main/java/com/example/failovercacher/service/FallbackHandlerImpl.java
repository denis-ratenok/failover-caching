package com.example.failovercacher.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FallbackHandlerImpl implements FallbackHandler<Object> {

    @Override
    public String getData() {
        return "Some default data";
    }
}
