package com.pyknic.servicekit.encode;

import com.google.gson.Gson;
import com.pyknic.servicekit.Service;
import com.pyknic.servicekit.ServiceException;

import java.util.Map;

/**
 * An encoder that parses the result of a service to a javascript that
 * calls a specific callback method with the response as a javascript
 * object parameter.
 * <p>
 * This class should not be instantiated directly but passed as a class
 * reference to the {@link Service} annotation to be instantiated
 * through reflection.
 * <p>
 * This class is stateless and instances can therefore safely be shared.
 *
 * @author  Emil Forslund
 * @see     {@url https://en.wikipedia.org/wiki/JSONP}
 */
public final class JsonpEncoder implements Encoder {

    private final static String MIME = "text/javascript";

    @Override
    public <T> String apply(Map<String, Object> params, T response) throws ServiceException {
        final Gson gson = new Gson();
        try {
            @SuppressWarnings("unchecked") // Throws a ServiceException instead
            final String callback = (String) params.getOrDefault("callback", "callback");
            return callback + "(" + gson.toJson(response) + ");";
        } catch (ClassCastException ex) {
            throw new ServiceException("Param 'callback' must be of type 'String' when parsing JSONP.", ex);
        }
    }

    @Override
    public String getMimeType() {
        return MIME;
    }

    public JsonpEncoder() {}
}