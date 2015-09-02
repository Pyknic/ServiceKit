package com.pyknic.servicekit.cache;

import java.util.function.Function;

/**
 * Describes a cache that can be used to minimize load on the server. The most
 * basic cache is the {@link NoCache} that simply calculates the value again for
 * every request.
 * 
 * @author Emil Forslund
 */
public interface Cache {
    
    /**
     * Queries the cache with the specified request. If the cahce already 
     * contains an answer for the request, it should be returned. Else, the
     * responder can be used to produce one.
     * 
     * @param request    the full request
     * @param responder  that can produce an answer
     * @return           an answer for the request (not null)
     */
    String get(String request, Function<String, String> responder);
}