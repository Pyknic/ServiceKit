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

import com.google.gson.Gson;
import com.pyknic.servicekit.cache.Cache;
import com.pyknic.servicekit.encode.Encoder;
import fi.iki.elonen.NanoHTTPD.Response.Status;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import java.util.Optional;
import static java.util.stream.Collectors.joining;

/**
 * A representation of a {@code Method} that contains additional methods for
 * working as a http service. A hook can only be instantiated by the server
 * that it serves.
 *
 * @author     Emil Forslund
 * @param <T>  the server type
 */
public final class ServiceHook<T extends HttpServer> {
    
    private final T server;
    private final Method method;
    private final Cache cache;

    static <T extends HttpServer> ServiceHook<T> create(T servlet, Method method) {
        return new ServiceHook<>(servlet, method);
    }

    public String getName() {
        return method.getName().toLowerCase();
    }

    public Encoder getEncoder() throws ServiceException {
        try {
            return getService().encoder().newInstance();
        } catch (NullPointerException ex) {
            throw new ServiceException(
                "Encoder '" + getService().encoder().getSimpleName() +
                "' specified in service '" + method.getName() +
                "' in server '" + server.getClass().getSimpleName() +
                "' with service signature '" + getSignature() +
                "' is null.",
                ex
            );
        } catch (IllegalAccessException | InstantiationException ex) {
            throw new ServiceException(
                "Encoder '" + getService().encoder().getSimpleName() +
                "' specified in service '" + method.getName() +
                "' in server '" + server.getClass().getSimpleName() +
                "' with service signature '" + getSignature() +
                "' is not instantiatable using it's default constructor.",
                ex
            );
        }
    }
    
    public Cache getCache() {
        return cache;
    }

    public Service getService() throws ServiceException {
        final Service service = method.getAnnotation(Service.class);

        if (service == null) {
            throw new ServiceException(
                "Parameter names are not present in build. Enable parameter " +
                "names in project pom.xml-file or specify the names as arguments " +
                "to the 'Service'-annotation to use ServiceKit."
            );
        }

        return service;
    }

    @Override
    public String toString() {
        return getName() + "::" + getSignature();
    }

    String call(Map<String, String> params) throws ServiceException {
        final Gson gson = new Gson();
        final Map<String, Object> args = new LinkedHashMap<>();
        
        Stream.of(method.getParameters())
            .forEachOrdered(p -> {
                final Argument arg = toArgument(p, params, gson);
                args.put(arg.name, arg.value);
            });

        final Object result;
        try {
            result = method.invoke(server, args.values().toArray());
        } catch (InvocationTargetException ex) {
            final Throwable thrw = Optional.ofNullable(ex.getCause()).orElse(ex);
            if (thrw instanceof HttpResponseException) {
                @SuppressWarnings("unchecked")
                final HttpResponseException httpThrw = (HttpResponseException) thrw;
                throw httpThrw;
            } else {
                thrw.printStackTrace();
                throw new HttpResponseException(Status.INTERNAL_ERROR, 
                    "Service '" + method.getName() +
                    "' in server '" + server.getClass().getSimpleName() +
                    "' casted an exception of type '" + 
                    thrw.getClass().getSimpleName() + "'."
                );
            }
        } catch (IllegalAccessException | IllegalArgumentException ex) {
            throw new ServiceException(
                "Service '" + method.getName() +
                "' in server '" + server.getClass().getSimpleName() +
                "' could not be executed with signature '" + getSignature() + 
                "' and values '" + args.values().toString() + "'.",
                ex
            );
        }

        return getEncoder().apply(args, result);
    }

    private String getSignature() {
        return "(" + Stream.of(method.getParameterTypes())
            .map(Class::getSimpleName)
            .collect(joining(", ")) + ")";
    }

    private Argument toArgument(Parameter param, Map<String, String> params, Gson gson) throws ServiceException {

        final String paramName;
        
        if (param.isNamePresent()) {
            paramName = param.getName().toLowerCase();
        } else {
            final String[] paramNames = getService().value();
            final int index = Arrays.asList(method.getParameters()).indexOf(param);

            if (index >= 0 && index < paramNames.length) {
                paramName = paramNames[index].toLowerCase();
            } else {
                throw new ServiceException(
                    "Parameter names are not present in build and does not " +
                    "match any given as annotation argument."
                );
            }
        }
        
        return params.entrySet().stream()
            .filter(e -> paramName.equals(e.getKey().toLowerCase()))
            .map(Map.Entry::getValue)
            .findAny()
            .map(json -> gson.fromJson(json, param.getParameterizedType()))
            .map(obj -> new Argument(paramName, obj))
            .orElseGet(() -> {
                if (Optional.class.isAssignableFrom(param.getType())) {
                    return new Argument(paramName, Optional.empty());
                } else {
                    throw new ServiceException(
                        "Parameter '" + paramName +
                        "' of type '" + param.getType().getSimpleName() +
                        "' is missing in call to service '" + method.getName() + "'."
                    );
                }
            });
    }

    private static class Argument {
        private final String name;
        private final Object value;

        private Argument(String name, Object value) {
            this.name  = name;
            this.value = value;
        }
    }

    private ServiceHook(T server, Method method) throws ServiceException {
        this.server = requireNonNull(server);
        this.method = requireNonNull(method);
        
        try {
            this.cache  = getService().cache().newInstance();
        } catch (InstantiationException | IllegalAccessException ex) {
            throw new ServiceException(
                "Could not instantiate suggested cache '" +
                getService().cache().getSimpleName()
                + "'. Maybe the default constructor is not accessible?", 
                ex
            );
        }
    }
}