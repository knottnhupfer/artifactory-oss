package org.artifactory.addon.npm;

import org.springframework.security.web.savedrequest.Enumerator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.util.Collections;
import java.util.Enumeration;

import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class NpmAuditRequestWrapper extends HttpServletRequestWrapper {
    private Enumerator<String> contentTypeHeadersResult = null;

    public NpmAuditRequestWrapper(HttpServletRequest request) {
        super(request);
        Enumeration<String> headers = super.getHeaders(CONTENT_TYPE);
        while(headers.hasMoreElements()){
            if (headers.nextElement().equalsIgnoreCase(APPLICATION_JSON)) {
                contentTypeHeadersResult = new Enumerator<>(Collections.singletonList(APPLICATION_JSON));
                break;
            }
        }
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (contentTypeHeadersResult != null && CONTENT_TYPE.equalsIgnoreCase(name)) {
            return contentTypeHeadersResult;
        }
        return super.getHeaders(name);
    }

    @Override
    public String getHeader(String name) {
        if (contentTypeHeadersResult != null && CONTENT_TYPE.equalsIgnoreCase(name)) {
            return APPLICATION_JSON;
        }
        return super.getHeader(name);
    }
}
