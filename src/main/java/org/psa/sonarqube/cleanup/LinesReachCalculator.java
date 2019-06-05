package org.psa.sonarqube.cleanup;

import org.psa.sonarqube.cleanup.config.Config;
import org.psa.sonarqube.cleanup.rest.model.License;

public class LinesReachCalculator {

    private LinesReachCalculator() {
        super();
    }

    static long calculateLinesToReach(Config config, License license) {

        int thresholdCoeff = config.getThresholdCoeff();
        long numberLocAdd = config.getNumberLocAdd();

        long maxLoc = license.getMaxLoc();
        long loc = license.getLoc();
        long remainingLocThreshold = license.getRemainingLocThreshold();

        long loc2reach = (remainingLocThreshold * thresholdCoeff + numberLocAdd) + loc - maxLoc;

        if (loc2reach < 0) {
            loc2reach = 0;
        }
        if (loc2reach > maxLoc) {
            loc2reach = maxLoc;
        }
        return loc2reach;
    }
}
