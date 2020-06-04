package org.artifactory.request.gzip;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;
import java.io.IOException;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Provider
public class GZIPReaderInterceptor implements ReaderInterceptor {

    @Override
    public Object aroundReadFrom(ReaderInterceptorContext context) throws IOException, WebApplicationException {
        MultivaluedMap<String, String> headers = context.getHeaders();
        if (headers == null) {
            return context.proceed();
        }
        String encodingKey = "Content-Encoding";
        List<String> encoding = headers.get(encodingKey);
        if (encoding == null) { //npm older versions used lower case header
            encodingKey = "content-encoding";
            encoding = headers.get(encodingKey);
            if (encoding == null) {
                return context.proceed();
            }
        }
        if (encoding.size() == 1 && "gzip".equalsIgnoreCase(encoding.get(0))) {
            headers.remove(encodingKey);
            context.setInputStream(new GZIPInputStream(context.getInputStream()));
        }
        return context.proceed();
    }
}
