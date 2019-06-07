package org.psa.sonarqube.cleanup.rest;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.jersey.message.internal.FormProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractClient {

    private static final Logger LOG = LoggerFactory.getLogger(AbstractClient.class);

    private String url;

    private String xsrfToken;

    protected AbstractClient() {
        super();
    }

    protected void setUrl(String url) {
        this.url = url;
    }

    protected <T> T get(String path, MultivaluedMap<String, Object> headers, Class<T> entityResponse) {
        return call(path, headers, null, entityResponse, false);
    }

    protected <T> T get(String path, MultivaluedMap<String, Object> headers, Class<T> entityResponse, boolean wrapRoot) {
        return call(path, headers, null, entityResponse, wrapRoot);
    }

    protected <T> T post(String path, Object entityRequest, Class<T> entityResponse) {
        return post(path, null, entityRequest, entityResponse);
    }

    protected <T> T post(String path, MultivaluedMap<String, Object> headers, Object entityRequest, Class<T> entityResponse) {
        return call(path, headers, entityRequest, entityResponse, false);
    }

    private <T> T call(String path, MultivaluedMap<String, Object> headers, Object entityRequest, Class<T> entityResponse, boolean wrapRoot) {
        long start = System.currentTimeMillis();
        try {
            LOG.debug("Call URL: {}/{}", url, path);
            if (StringUtils.isBlank(url)) {
                throw new UnsupportedOperationException("Please use 'setUrl(...)' before using this client");
            }

            Client client = ClientBuilder.newClient();
            if (wrapRoot) {
                client = client.register(ObjectMapperContextResolver.class);
            }
            client.register(FormProvider.class);

            // Manage query parameters for correct encoding
            String pathNoParams = path;
            int separator = pathNoParams.indexOf('?');
            if (separator > 0) {
                pathNoParams = pathNoParams.substring(0, separator);
            }
            WebTarget webTarget = client.target(url).path(pathNoParams);
            if (separator > 0) {
                for (String p : path.substring(separator + 1).split("&")) {
                    int equal = p.indexOf('=');
                    webTarget = webTarget.queryParam(p.substring(0, equal), p.substring(equal + 1));
                }
            }

            Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON_TYPE).headers(addXsrfTokenToHeaders(headers));
            Response response = null;
            if (entityRequest == null) {
                response = invocationBuilder.get();
            } else {
                response = invocationBuilder.post(Entity.entity(entityRequest, MediaType.APPLICATION_FORM_URLENCODED_TYPE));
            }
            if (Response.Status.OK.getStatusCode() != response.getStatus() && Response.Status.NO_CONTENT.getStatusCode() != response.getStatus()) {
                String content = IOUtils.toString((InputStream) response.getEntity(), Charset.defaultCharset());
                if (StringUtils.isNoneBlank(content)) {
                    content = " / Content: " + content;
                }
                throw new UnsupportedOperationException(
                        String.format("Unsupported status code: %s %s%s", response.getStatus(), response.getStatusInfo().getReasonPhrase(), content));
            }
            storeXsrfToken(response);
            return response.readEntity(entityResponse);
        } catch (IOException e) {
            throw new UnsupportedOperationException(e);
        } finally {
            LOG.debug("Call URL time elaps ms: {}", System.currentTimeMillis() - start);
        }
    }

    private void storeXsrfToken(Response response) {
        Cookie token = response.getCookies().get("XSRF-TOKEN");
        if (token != null && StringUtils.isNotBlank(token.getValue())) {
            xsrfToken = token.getValue();
        }
    }

    private MultivaluedMap<String, Object> addXsrfTokenToHeaders(MultivaluedMap<String, Object> headers) {
        MultivaluedMap<String, Object> headersWithToken = headers;
        if (StringUtils.isNotBlank(xsrfToken)) {
            if (headersWithToken == null) {
                headersWithToken = new MultivaluedHashMap<>();
            }
            headersWithToken.add("X-XSRF-TOKEN", xsrfToken);
        }
        return headersWithToken;
    }

}
