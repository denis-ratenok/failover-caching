package com.example.failovercacher.service;

import com.example.failovercacher.exeption.MissedCacheException;
import com.example.failovercacher.parameter.SuitableDataAware;
import com.example.failovercacher.parameter.ClearCacheAware;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class FailoverCacherImpl implements FailoverCacher {
    private final CacheManager cacheManager;

    @Override
    public <T> T getSuitableData(SuitableDataAware<?> suitableDataAware) {
        final Cache cache = cacheManager.getCache(suitableDataAware.getCachePrefix());
        if (cache == null) {
            throw new MissedCacheException(suitableDataAware.getCachePrefix() + suitableDataAware.getCacheId());
        }

        final String cacheId = suitableDataAware.getCacheId();
        final LocalDateTime refreshTime = Optional.ofNullable(cache.get(cacheId, LocalDateTime.class))
                                              .orElse(LocalDateTime.MIN);

        if (refreshTime.isBefore(refreshTime)) {
            final Optional<T> valueFromCache = fetchFromCache(suitableDataAware, cache);
            if (valueFromCache.isPresent()) {
                return valueFromCache.get();
            }
        }

        return fetchFromServiceAndUpdateCache(suitableDataAware, cache, cacheId, refreshTime);
    }

    @Override
    public void clearCache(ClearCacheAware clearCacheAware) {
        final Cache cache = Optional.ofNullable(cacheManager.getCache(clearCacheAware.getCachePrefix()))
                                .orElseThrow(
                                    () -> new MissedCacheException(clearCacheAware.getCachePrefix() + clearCacheAware.getCacheId())
                                );

        final String refreshTimeKey = clearCacheAware.getCacheId();
        cache.evict(refreshTimeKey);
    }

    private static <T> Optional<T> fetchFromCache(SuitableDataAware<?> suitableDataAware, Cache cache) {
        return (Optional<T>) Optional.ofNullable(cache.get(suitableDataAware.getCacheId()))
                                 .map(Cache.ValueWrapper::get);
    }

    private <T> T fetchFromServiceAndUpdateCache(SuitableDataAware<?> suitableDataAware, Cache cache,
                                                 String crtKey, LocalDateTime refreshTime) {
        T data;
        try {
            data = fetchFromTargetService(suitableDataAware);
        } catch (Exception ex) {
            log.warn(ex.getMessage(), suitableDataAware.getCachePrefix(), suitableDataAware.getCacheId());
            data = fetchFromCacheOrFallbackHandler(suitableDataAware, cache);
        }

        cache.put(suitableDataAware.getCacheId(), data);
        cache.put(crtKey, refreshTime.plusSeconds(suitableDataAware.getRefreshPeriodSeconds()));

        return data;
    }

    private static <T> T fetchFromTargetService(SuitableDataAware<?> suitableDataAware) {
        return (T) suitableDataAware.getServiceHandler().get();
    }

    private <T> T fetchFromCacheOrFallbackHandler(SuitableDataAware suitableDataAware, Cache cache) {
        return (T) fetchFromCache(suitableDataAware, cache).orElseGet(() -> suitableDataAware.getFallbackHandler().getData());
    }
}
