package com.pyknic.servicekit.encode;

import com.google.gson.Gson;
import com.pyknic.servicekit.Service;

import java.util.Map;

/**
 * An encoder that parses the result of a service to the json format.
 * <p>
 * This class should not be instantiated directly but passed as a class
 * reference to the {@link Service} annotation to be instantiated
 * through reflection.
 * <p>
 * This class is stateless and instances can therefore safely be shared.
 *
 * @author Emil Forslund
 */
public final class JsonEncoder implements Encoder {

    private final static String MIME = "application/json";

    @Override
    public <T> String apply(Map<String, Object> params, T response) {
        final Gson gson = new Gson();
        return gson.toJson(response);
    }

    @Override
    public String getMimeType() {
        return MIME;
    }

    public JsonEncoder() {}
}