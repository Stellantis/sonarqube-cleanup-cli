package org.psa.sonarqube.cleanup;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;

import org.junit.Test;

public class SonarQubeCleanupCliTest extends AbstractWireMock {

    private static final String URL_COMPONENTS_SEARCH_PROJECTS = "/api/components/search_projects.*";
    private static final String URL_MEASURES_COMPONENTS = "/api/measures/component.*";
    private static final String URL_PROJECTS_DELETE = "/api/projects/delete.*";

    @Test
    public void testNoLineToReach() {
        mockEndoints();
        SonarQubeCleanupCli.main(new String[] { "-h", "http://localhost:" + server.port(), "-l", "admin" });

        verify(0, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(0, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));

    }

    @Test
    public void testOneProjectDeletion() {
        mockEndoints();
        SonarQubeCleanupCli.main(new String[] { "-h", "http://localhost:" + server.port(), "-l", "admin", "-y", "-n", "1" });

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(1, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(1, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    @Test
    public void testTwoProjectDeletion() {
        mockEndoints();
        SonarQubeCleanupCli.main(new String[] { "-h", "http://localhost:" + server.port(), "-l", "admin", "-y", "-n", "2000" });

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(2, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(2, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    @Test
    public void testAllProjectDeletion() {
        mockEndoints();
        SonarQubeCleanupCli.main(new String[] { "-h", "http://localhost:" + server.port(), "-l", "admin", "-y", "-n", "10000000" });

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(9, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(9, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    @Test
    public void testDryRun() {
        mockEndoints();
        SonarQubeCleanupCli.main(new String[] { "-h", "http://localhost:" + server.port(), "-l", "admin", "-y", "-d", "-n", "1" });

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(1, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(0, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    private void mockEndoints() {
        stubFor(get("/api/editions/show_license").willReturn(aResponse().withHeader(HCTKEY, HCTJSON).withBodyFile("editions.show_license.json")));
        stubFor(get(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS))
                .willReturn(aResponse().withHeader(HCTKEY, HCTJSON).withBodyFile("components.search_projects.json")));
        stubFor(get(urlMatching(URL_MEASURES_COMPONENTS))
                .willReturn(aResponse().withHeader(HCTKEY, HCTJSON).withBodyFile("measures.component.1.json")));
        stubFor(post(urlMatching(URL_PROJECTS_DELETE)));
    }
}
