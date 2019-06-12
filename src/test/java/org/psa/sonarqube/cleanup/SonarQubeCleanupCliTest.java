package org.psa.sonarqube.cleanup;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.contrib.java.lang.system.TextFromStandardInputStream.emptyStandardInputStream;

import javax.ws.rs.core.Response;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.TextFromStandardInputStream;

public class SonarQubeCleanupCliTest extends AbstractWireMock {

    private static final String URL_COMPONENTS_SEARCH_PROJECTS = "/api/components/search_projects.*";
    private static final String URL_MEASURES_COMPONENTS = "/api/measures/component.*";
    private static final String URL_PROJECTS_DELETE = "/api/projects/delete";

    private static final String LOCALHOST = "http://localhost:";
    private static final String USER = "admin";
    private static final String PASSWORD = "foobar"; // NOSONAR : UT password

    @Rule
    public final TextFromStandardInputStream systemInMock = emptyStandardInputStream();

    @Test
    public void testConstructor() {
        new SonarQubeCleanupCli(); // NOSONAR : For better code-coverage

        verify(0, anyRequestedFor(urlMatching(URL_AUTHENT)));
    }

    @Test
    public void testNoLineToReach() {
        mockEndoints();
        SonarQubeCleanupCli.main(new String[] { "-h", LOCALHOST + server.port(), "-l", USER, "-p", PASSWORD, });

        verify(0, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(0, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
    }

    @Test
    public void testOneProjectDeletion() {
        mockEndoints();
        SonarQubeCleanupCli.main(new String[] { "-h", LOCALHOST + server.port(), "-l", USER, "-p", PASSWORD, "-y", "-n", "1" });

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(1, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(1, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    @Test
    public void testOneProjectDeletionWithYes() {
        mockEndoints();
        systemInMock.provideLines("y");
        SonarQubeCleanupCli.main(new String[] { "-h", LOCALHOST + server.port(), "-l", USER, "-p", PASSWORD, "-n", "1" });

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(1, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(1, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    @Test
    public void testOneProjectDeletionWithPassword() {
        mockEndoints();
        systemInMock.provideLines(PASSWORD);
        SonarQubeCleanupCli.main(new String[] { "-h", LOCALHOST + server.port(), "-l", USER, "-y", "-n", "1" });

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(1, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(1, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    @Test
    public void testOneProjectDeletionWithNo() {
        mockEndoints();
        systemInMock.provideLines("N");
        SonarQubeCleanupCli.main(new String[] { "-h", LOCALHOST + server.port(), "-l", USER, "-p", PASSWORD, "-n", "1" });

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(1, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(0, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    @Test
    public void testTwoProjectDeletion() {
        mockEndoints();
        SonarQubeCleanupCli.main(new String[] { "-h", LOCALHOST + server.port(), "-l", USER, "-p", PASSWORD, "-y", "-n", "2000" });

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(2, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(2, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    @Test
    public void testAllProjectDeletion() {
        mockEndoints();
        SonarQubeCleanupCli.main(new String[] { "-h", LOCALHOST + server.port(), "-l", USER, "-p", PASSWORD, "-y", "-n", "10000000" });

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(9, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(9, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    @Test
    public void testUserToken() {
        mockEndoints();
        SonarQubeCleanupCli
                .main(new String[] { "-h", LOCALHOST + server.port(), "-l", "0000000000000000000000000000000000000000", "-y", "-n", "10000000" });

        verify(0, anyRequestedFor(urlMatching(URL_AUTHENT)));

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(9, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(9, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    @Test
    public void testDryRun() {
        mockEndoints();
        SonarQubeCleanupCli.main(new String[] { "-h", LOCALHOST + server.port(), "-l", USER, "-p", PASSWORD, "-y", "-d", "-n", "1" });

        verify(1, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
        verify(1, anyRequestedFor(urlMatching(URL_MEASURES_COMPONENTS)));
        verify(0, anyRequestedFor(urlMatching(URL_PROJECTS_DELETE)));
    }

    @Test
    public void testErrorParsingCommand() {
        // Should not throw exception when parsing error command
        SonarQubeCleanupCli.main(new String[] {});
        verify(0, anyRequestedFor(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS)));
    }

    private void mockEndoints() {
        stubFor(get("/api/editions/show_license").willReturn(aResponse().withHeader(HCTKEY, HCTJSON).withBodyFile("editions.show_license.json")));
        stubFor(get(urlMatching(URL_COMPONENTS_SEARCH_PROJECTS))
                .willReturn(aResponse().withHeader(HCTKEY, HCTJSON).withBodyFile("components.search_projects.json")));
        stubFor(get(urlMatching(URL_MEASURES_COMPONENTS))
                .willReturn(aResponse().withHeader(HCTKEY, HCTJSON).withBodyFile("measures.component.1.json")));
        stubFor(post(urlMatching(URL_PROJECTS_DELETE)).willReturn(aResponse().withStatus(Response.Status.NO_CONTENT.getStatusCode())));
    }
}
