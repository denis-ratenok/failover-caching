package com.example.failovercacher.service;

import com.example.failovercacher.parameter.SuitableDataAware;
import com.example.failovercacher.parameter.ClearCacheAware;

public interface FailoverCacher {
    <T> T getSuitableData(SuitableDataAware<?> value);
    void clearCache(ClearCacheAware value);
}
