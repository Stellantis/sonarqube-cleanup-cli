package org.psa.sonarqube.cleanup.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Measure {

    private String metric;
    private long value;

    public String getMetric() {
        return metric;
    }

    public long getValue() {
        return value;
    }
}
