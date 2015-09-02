package com.pyknic.servicekit.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * A {@link Cache} implementation that simly stores requests in a 
 * {@code HashMap} for 1 hour by default. Subclasses might have a different
 * life length.
 * 
 * @author Emil Forslund
 */
public class BasicCache implements Cache {
    
    public final static int ONE_HOUR = 1000 * 60 * 60;
    
    private final Map<String, CacheResult> cache;
    private final int expirationAge;
    
    public BasicCache() {
        cache         = new HashMap<>();
        expirationAge = ONE_HOUR;
    }
    
    protected BasicCache(int expirationAge) {
        this.cache         = new HashMap<>();
        this.expirationAge = expirationAge;
    }
    
    protected int getExpirationAge() {
        return expirationAge;
    }
    
    @Override
    public String get(String request, Function<String, String> responder) {
        CacheResult response = cache.get(request);
        
        if (response == null || response.hasExpired(System.currentTimeMillis())) {
            response = new CacheResult(
                responder.apply(request), 
                System.currentTimeMillis() + expirationAge
            );
            
            cache.put(request, response);
        }
        
        return response.value;
    }
    
    private final static class CacheResult {
        
        private final String value;
        private final long expirationTime;
        
        public CacheResult(String value, long expirationTime) {
            this.value          = value;
            this.expirationTime = expirationTime;
        }

        public boolean hasExpired(long now) {
            return expirationTime < now;
        }
    }
}