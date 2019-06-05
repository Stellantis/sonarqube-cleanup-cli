package org.psa.sonarqube.cleanup.rest.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class License {

    private long maxLoc;
    private long loc;
    private long remainingLocThreshold;

    public long getMaxLoc() {
        return maxLoc;
    }

    public long getLoc() {
        return loc;
    }

    public long getRemainingLocThreshold() {
        return remainingLocThreshold;
    }
}
