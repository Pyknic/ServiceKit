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

import com.pyknic.servicekit.cache.Cache;
import com.pyknic.servicekit.cache.NoCache;
import com.pyknic.servicekit.encode.Encoder;
import com.pyknic.servicekit.encode.JsonEncoder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates the specified method as a http-service that should be accessible
 * online. Service-methods should be members to a {@link HttpServer}-class.
 * 
 * @author Emil Forslund
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Service {
    /**
     * A list of the names of the parameters of this method. This will be used
     * to match GET parameters to specific input variables. Variable names can
     * not be safely retrieved using reflection since that information might be
     * removed during compilation.
     *
     * @return  a list of the names of the method parameters
     */
    String[] value() default {};

    /**
     * This value signals which {@link Encoder} to use when converting the output
     * value of the annoted service to a string that can be sent to the user.
     * The implementation must have a default constructor with no parameters so 
     * that it can be instantiated using reflection.
     * 
     * @return  the encoder type
     */
    Class<? extends Encoder> encoder() default JsonEncoder.class;
    
    /**
     * This value signals which {@link Cache} to use when storing responses
     * already calculated. The implementation must have a default constructor
     * with no parameters so that it can be instantiated using reflection.
     * 
     * @return  the cache type
     */
    Class<? extends Cache> cache() default NoCache.class;
}