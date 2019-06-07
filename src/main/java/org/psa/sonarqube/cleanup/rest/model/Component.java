package org.psa.sonarqube.cleanup.rest.model;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonRootName(value = "component")
public class Component {

    private String id;
    private String key;
    private String name;
    private String description;
    private Date analysisDate;
    private List<Measure> measures;

    public String getId() {
        return id;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Date getAnalysisDate() {
        return analysisDate;
    }

    public List<Measure> getMeasures() {
        return measures;
    }

    public long getNcloc() {
        if (getMeasures() != null && !getMeasures().isEmpty()) {
            for (Measure m : getMeasures()) {
                if ("ncloc".equals(m.getMetric())) {
                    return m.getValue();
                }
            }
        }
        return 0;
    }
}
