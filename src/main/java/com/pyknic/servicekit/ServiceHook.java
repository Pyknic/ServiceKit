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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import java.util.stream.Stream;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 *
 * @author Emil Forslund
 */
public class ServiceHook<T extends HttpServer> {
    
    private final T servlet;
    private final Method method;
    
    private ServiceHook(T servlet, Method method) {
        this.servlet = requireNonNull(servlet);
        this.method  = requireNonNull(method);
    }
    
    public String getName() {
        return method.getName().toLowerCase();
    }
    
    public String getSignature() {
        return "(" + Stream.of(method.getParameterTypes())
            .map(t -> t.getSimpleName())
            .collect(joining(", ")) + ")";
    }
    
    public String call(Map<String, String> params) throws ServiceException {

        final Gson gson = new Gson();
        final Object[] args = Stream.of(method.getParameters())
            .map(p -> {
                final Class<?> type = p.getType();
                final String jsonValue;
                try {
                    jsonValue = getArgument(p, params);
                } catch (ServiceException ex) {
                    throw new RuntimeException(ex);
                }
                final Object arg = gson.fromJson(jsonValue, type);
                return arg;
            })
            .toArray();

        final Object result;
        try {
            result = method.invoke(servlet, args);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new ServiceException(
                "Method '" + method.getName() + 
                "' in servlet '" + servlet.getClass().getSimpleName() + 
                "' could not be executed with signature '" + getSignature() + "'.");
        }
        
        return gson.toJson(result);
    }
    
    public static <T extends HttpServer> ServiceHook<T> create(T servlet, Method method) {
        return new ServiceHook<>(servlet, method);
    }

    private String getArgument(Parameter param, Map<String, String> params) throws ServiceException {

        final String paramName;
        
        if (param.isNamePresent()) {
            paramName = param.getName().toLowerCase();
        } else {
            method.getAnnotations();
            final Service service = method.getAnnotation(Service.class);

            if (service == null) {
                throw new ServiceException(
                    "Parameter names are not present in build. Enable parameter " +
                    "names in project pom.xml-file or specify the names as arguments " +
                    "to the 'Service'-annotation to use ServiceKit."
                );
            }
            
            final String[] paramNames = service.value();
            final int index = Arrays.asList(method.getParameters()).indexOf(param);

            if (index >= 0 && index < paramNames.length) {
                paramName = paramNames[index];
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
            .orElseThrow(() -> new ServiceException(
                "Parameter '" + paramName + 
                "' of type '" + param.getType().getSimpleName() + 
                "' is missing in call to service '" + method.getName() + "'."
            ));
    }

    @Override
    public String toString() {
        return getName() + " " + getSignature();
    }
}