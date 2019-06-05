
package org.psa.sonarqube.cleanup.rest;

import java.util.Base64;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;

import org.psa.sonarqube.cleanup.config.Config;
import org.psa.sonarqube.cleanup.rest.model.Component;
import org.psa.sonarqube.cleanup.rest.model.License;
import org.psa.sonarqube.cleanup.rest.model.SearchProjects;

public class SonarQubeClient extends AbstractClient {

    private String authorization;

    void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public static SonarQubeClient build(Config config) {
        SonarQubeClient client = new SonarQubeClient();
        client.setUrl(config.getHostUrl());
        String lp = config.getLogin() + ":" + config.getPassword();
        client.setAuthorization("Basic " + Base64.getEncoder().encodeToString(lp.getBytes()));
        return client;
    }

    private MultivaluedMap<String, Object> getHeadersAuthorization() {
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        headers.add("Authorization", this.authorization);
        return headers;
    }

    public License getLicence() {
        return get("api/editions/show_license", getHeadersAuthorization(), License.class);
    }

    public SearchProjects getProjectsOldMax500() {
        // We use internal API, because result can be sorted by analysisDate
        return get("api/components/search_projects?ps=500&f=analysisDate&s=analysisDate", getHeadersAuthorization(), SearchProjects.class);
    }

    public Component getProject(String key) {
        return get("api/measures/component?metricKeys=ncloc&componentKey=" + key, getHeadersAuthorization(), Component.class, true);
    }

    public void deleteProject(String key) {
        // 'api/projects/bulk_delete' not used because could be a little bit dangerous ...
        post("api/projects/delete?key=" + key, getHeadersAuthorization(), String.class);
    }

}
