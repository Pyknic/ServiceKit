/*
 * Copyright 2015 Emil Forslund.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.pyknic.servicekit;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.stream.Stream;

/**
 *
 * @author Emil Forslund
 */
public abstract class HttpServer {

    private final NanoHTTPD server;
    
    protected HttpServer(int port) {
        server = new NanoHTTPD(port) {

            @Override
            public Response serve(IHTTPSession session) {
                final URI uri;
                final Map<String, String> params;
                final String service;
                
                try {
                    uri     = new URI(session.getUri());
                    params  = session.getParms();
                    service = parseURIForService(uri);
                } catch (URISyntaxException | ServiceException ex) {
                    return new Response(Status.BAD_REQUEST, "text/plain", ex.getMessage());
                }
                
                final ServiceHook<HttpServer> hook;
                try {
                    hook = findCorrectHook(service);
                } catch (ServiceException ex) {
                    return new Response(Status.NOT_FOUND, "text/plain", ex.getMessage());
                }
                
                final String result;
                try {
                    result = hook.call(params);
                } catch (Exception ex) {
                    return new Response(Status.INTERNAL_ERROR, "text/plain", ex.getMessage());
                }
                
                return new Response(Status.OK, "application/json", result);
            }
        };
    }
    
    public HttpServer start() throws IOException {
        server.start();
        return this;
    }
    
    public HttpServer stop() {
        server.stop();
        return this;
    }
    
    private Stream<ServiceHook<HttpServer>> services() {
        return Stream.of(getClass().getMethods())
            .filter(m -> m.getAnnotation(Service.class) != null)
            .map(m -> ServiceHook.create(this, m));
    }
    
    private String parseURIForService(URI uri) throws ServiceException {
        return Stream.of(uri.getPath())
            .filter(p -> p != null)
            .flatMap(p -> Stream.of(p.split("/")))
            .filter(p -> !p.isEmpty())
            .findFirst()
            .orElseThrow(() -> new ServiceException(
                "No service specified in uri: '" + uri.toString() + "'."
            ));
    }
    
    private ServiceHook<HttpServer> findCorrectHook(String service) throws ServiceException {
        return services()
            .filter(sh -> sh.getName().equals(service))
            .findAny().orElseThrow(
                () -> new ServiceException(
                    "The specified service '" + service + "' could not be found."
                )
            );
    }
}