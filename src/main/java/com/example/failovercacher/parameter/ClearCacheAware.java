package com.example.failovercacher.parameter;

import lombok.Data;

@Data
public class ClearCacheAware {
    private String cachePrefix;
    private String cacheId;
}
