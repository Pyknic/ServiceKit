package com.pyknic.servicekit.encode;

import com.pyknic.servicekit.Service;
import com.pyknic.servicekit.ServiceException;

import java.util.Map;

/**
 * Encodes the result of a service method to a format that can be transmitted
 * over the web.
 * <p>
 * Implementations of this class are meant to be specified as a param to the
 * {@link Service} annotation. Implementations must have a default constructor
 * with no parameters to allow instantiation through reflection.
 *
 * @author  Emil Forslund
 */
public interface Encoder {
    /**
     * Applies this encoder to the specified response. The params given to the
     * service that generated the response is also supplied.
     *
     * @param params             the params given to the {@link Service}
     * @param response           the response to encode
     * @param <T>                the type of the response
     * @return                   the encoded response
     * @throws ServiceException  if the encoding could not be completed
     */
    <T> String apply(Map<String, Object> params, T response) throws ServiceException;

    /**
     * Returns the mime type that responses encoded with this encoder
     * should have.
     *
     * @return  the mimetype
     */
    String getMimeType();
}