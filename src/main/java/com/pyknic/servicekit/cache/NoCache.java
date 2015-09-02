package com.pyknic.servicekit.cache;

import java.util.function.Function;

/**
 * A {@link Cache} implementation that doesn't do any caching but calculates the
 * result for every request.
 * 
 * @author Emil Forslund
 */
public final class NoCache implements Cache {
    
    @Override
    public String get(String request, Function<String, String> responder) {
        return responder.apply(request);
    }
}