package org.psa.sonarqube.cleanup;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import org.junit.Rule;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

public abstract class AbstractWireMock {

    protected static final String HCTKEY = "Content-Type";
    protected static final String HCTJSON = "application/json";

    @Rule
    public WireMockRule server = new WireMockRule(options().dynamicPort().portNumber());

}
