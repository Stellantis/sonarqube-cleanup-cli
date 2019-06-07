package org.psa.sonarqube.cleanup;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import org.junit.Before;
import org.junit.Rule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public abstract class AbstractWireMock {

    protected static final String HCTKEY = "Content-Type";
    protected static final String HCTJSON = "application/json";

    protected static final String URL_AUTHENT = "/api/authentication/login";

    @Rule
    public WireMockRule server = new WireMockRule(options().dynamicPort().portNumber());

    @Before
    public void setUp() {
        stubFor(post(URL_AUTHENT).willReturn(aResponse().withHeader("Set-Cookie", "XSRF-TOKEN=xxx")));
    }
}
