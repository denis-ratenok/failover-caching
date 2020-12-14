package com.example.failovercacher.parameter;

import com.example.failovercacher.service.FallbackHandler;
import lombok.Data;

import java.util.function.Supplier;

@Data
public class SuitableDataAware<T> {
    public String cachePrefix;
    public String cacheId;
    public long refreshPeriodSeconds;
    public Supplier<T> serviceHandler;
    public FallbackHandler<?> fallbackHandler;
}
