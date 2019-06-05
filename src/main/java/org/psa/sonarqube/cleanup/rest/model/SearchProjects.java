package org.psa.sonarqube.cleanup.rest.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchProjects {

    List<Component> components;

    public List<Component> getComponents() {
        return components;
    }
}
