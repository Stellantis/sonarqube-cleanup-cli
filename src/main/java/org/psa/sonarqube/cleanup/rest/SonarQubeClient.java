
package org.psa.sonarqube.cleanup.rest;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;

import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.psa.sonarqube.cleanup.config.Config;
import org.psa.sonarqube.cleanup.rest.model.Component;
import org.psa.sonarqube.cleanup.rest.model.License;
import org.psa.sonarqube.cleanup.rest.model.SearchProjects;

public class SonarQubeClient extends AbstractClient {

    private static final List<Response.Status> PROJECT_DELETION_SUPPORTED_STATUS = Arrays.asList(Response.Status.NO_CONTENT,
            Response.Status.BAD_GATEWAY);

    private String authorization;

    void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public static SonarQubeClient build(Config config) {
        SonarQubeClient client = new SonarQubeClient();
        client.setUrl(config.getHostUrl());
        // Some API requires basicAuth (api/editions/show_license, api/measures/component), some other X-XSRF-TOKEN (api/projects/delete), so use both
        String password = StringUtils.defaultIfBlank(config.getPassword(), "");
        String lp = config.getLogin() + ":" + password;
        client.setAuthorization("Basic " + Base64.getEncoder().encodeToString(lp.getBytes()));
        if (!config.isLoginUserToken()) {
            Form form = new Form();
            form.param("login", config.getLogin());
            form.param("password", password);
            client.post("api/authentication/login", form, String.class);
        }
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
        Form form = new Form();
        form.param("project", key);
        post("api/projects/delete", getHeadersAuthorization(), form, String.class, PROJECT_DELETION_SUPPORTED_STATUS);
    }

}
