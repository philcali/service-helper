package me.philcali.service.reflection.decoder;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class CachingParamDecoder implements IParamDecoder {
    private static class CachedItem {
        private final long ttl;
        private final String cacheKey;
        private Map<String, String> item;

        public CachedItem(String cacheKey, long ttl, final Map<String, String> item) {
            this.cacheKey = cacheKey;
            this.ttl = ttl;
            this.item = item;
        }

        @Override
        public boolean equals(Object obj) {
            if (Objects.isNull(obj) || !(obj instanceof CachedItem)) {
                return false;
            }

            final CachedItem item = (CachedItem) obj;
            return Objects.equals(cacheKey, item.getCacheKey());
        }

        public String getCacheKey() {
            return cacheKey;
        }

        public Map<String, String> getItem() {
            return item;
        }

        public long getTtl() {
            return ttl;
        }

        @Override
        public int hashCode() {
            return Objects.hash(cacheKey);
        }

        public boolean isExpired(final long checkedTime) {
            return ttl <= checkedTime;
        }
    }

    private static final long DEFAULT_TTL = TimeUnit.MINUTES.toMillis(5);

    private final long ttlTime;
    private final Map<String, CachedItem> cache;
    private final IParamDecoder decoder;

    public CachingParamDecoder(final IParamDecoder decoder) {
        this(decoder, DEFAULT_TTL);
    }

    public CachingParamDecoder(final IParamDecoder decoder, final long ttlTime) {
        this.decoder = decoder;
        this.ttlTime = ttlTime;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public Map<String, String> decode(final String data) {
        return cache.compute(data, (cacheKey, item) -> {
            if (item == null || item.isExpired(System.currentTimeMillis())) {
                return new CachedItem(cacheKey, ttlTime, decoder.decode(data));
            } else {
                return item;
            }
        }).getItem();
    }
}
