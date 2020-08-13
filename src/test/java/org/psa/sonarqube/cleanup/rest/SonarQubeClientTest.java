package org.psa.sonarqube.cleanup.rest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import java.util.Date;

import javax.ws.rs.core.Response;

import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;
import org.psa.sonarqube.cleanup.AbstractWireMock;
import org.psa.sonarqube.cleanup.config.Config;
import org.psa.sonarqube.cleanup.rest.model.Component;
import org.psa.sonarqube.cleanup.rest.model.License;
import org.psa.sonarqube.cleanup.rest.model.SearchProjects;

public class SonarQubeClientTest extends AbstractWireMock {

    private static final String PROJECT_KEY = "com.company:project1";

    @Test
    public void testStrangeToken() {
        stubFor(post(URL_AUTHENT).willReturn(aResponse().withHeader("Set-Cookie", "XSRF-TOKEN=")));
        mockClient();
        verify(1, anyRequestedFor(urlMatching(URL_AUTHENT)));
    }

    @Test
    public void testHeaderNull() {
        final String urlLicense = "/api/editions/show_license";
        stubFor(get(urlLicense).willReturn(aResponse().withHeader(HCTKEY, HCTJSON).withBodyFile("editions.show_license.json")));
        SonarQubeClient client = mockClient();
        // Force header null to verify correct behavior
        client.get(urlLicense, null, License.class);
        verify(1, anyRequestedFor(urlMatching(URL_AUTHENT)));
        verify(1, anyRequestedFor(urlMatching(urlLicense)));
    }

    @Test
    public void testBadAuth() {
        stubFor(post(URL_AUTHENT).willReturn(aResponse().withStatus(Response.Status.UNAUTHORIZED.getStatusCode())));
        try {
            mockClient();
            Assert.fail("Bad password");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("401 Unauthorized"));
        }
    }

    @Test
    public void testBadAuthWithFakeContent() {
        stubFor(post(URL_AUTHENT).willReturn(aResponse().withStatus(Response.Status.UNAUTHORIZED.getStatusCode()).withBody("Invalid password")));
        try {
            mockClient();
            Assert.fail("Bad password");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("401 Unauthorized"));
        }
    }

    @Test
    public void testNoUrl() {
        SonarQubeClient client = new SonarQubeClient();
        try {
            client.getLicence();
            Assert.fail("URL not set");
        } catch (UnsupportedOperationException e) {
            Assert.assertTrue(e.getMessage().contains("Please use 'setUrl(...)' before using this client"));
        }
    }

    @Test
    public void testShowLicence() {
        stubFor(get("/api/editions/show_license").willReturn(aResponse().withHeader(HCTKEY, HCTJSON).withBodyFile("editions.show_license.json")));
        SonarQubeClient client = mockClient();
        License license = client.getLicence();

        Assert.assertEquals(10000000, license.getMaxLoc());
        Assert.assertEquals(9000000, license.getLoc());
        Assert.assertEquals(500000, license.getRemainingLocThreshold());
    }

    @Test
    public void testSearchProjects() {
        stubFor(get(urlEqualTo("/api/components/search_projects?ps=500&f=analysisDate&s=analysisDate"))
                .willReturn(aResponse().withHeader(HCTKEY, HCTJSON).withBodyFile("components.search_projects.json")));
        SonarQubeClient client = mockClient();
        SearchProjects searchProjects = client.getProjectsOldMax500();

        Assert.assertEquals(9, searchProjects.getComponents().size());
        Date previous = new Date(0);
        for (Component c : searchProjects.getComponents()) {
            Date current = c.getAnalysisDate();
            Assert.assertTrue(String.format("Previous: %s / Current: %s", previous, current), current.after(previous));
            previous = current;
        }
    }

    @Test
    public void testComponentDetail() {
        stubFor(get(urlEqualTo("/api/measures/component?metricKeys=ncloc&componentKey=com.company%3Aproject1"))
                .willReturn(aResponse().withHeader(HCTKEY, HCTJSON).withBodyFile("measures.component.1.json")));
        SonarQubeClient client = mockClient();
        Component project = client.getProject(PROJECT_KEY);
        Assert.assertEquals("AVxxxxxxxxxxxxxxxxx1", project.getId());
        Assert.assertEquals(PROJECT_KEY, project.getKey());
        Assert.assertEquals("Mock project 1", project.getName());
        Assert.assertEquals("Mock project 1 description", project.getDescription());
        Assert.assertEquals(1042, project.getNcloc());
    }

    @Test
    public void testComponentDetailNoLoC() {
        stubFor(get(urlEqualTo("/api/measures/component?metricKeys=ncloc&componentKey=com.company%3Aproject1"))
                .willReturn(aResponse().withHeader(HCTKEY, HCTJSON).withBodyFile("measures.component.0.json")));
        SonarQubeClient client = mockClient();
        Component project = client.getProject(PROJECT_KEY);
        Assert.assertEquals("AVxxxxxxxxxxxxxxxxx1", project.getId());
        Assert.assertEquals(PROJECT_KEY, project.getKey());
        Assert.assertEquals("Mock project 1", project.getName());
        Assert.assertEquals("Mock project 1 description", project.getDescription());
        Assert.assertEquals(0, project.getNcloc());
    }

    private SonarQubeClient mockClient() {
        try {
            Config config = new Config(new String[] { "-h", "http://localhost:" + server.port(), "-l", "admin" });
            return SonarQubeClient.build(config);
        } catch (ParseException e) {
            throw new UnsupportedOperationException(e);
        }
    }

}
