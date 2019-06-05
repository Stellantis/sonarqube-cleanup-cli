
package org.psa.sonarqube.cleanup;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;
import org.psa.sonarqube.cleanup.config.Config;
import org.psa.sonarqube.cleanup.rest.model.License;

public class LinesReachCalculatorTest {

    @Test
    public void test() {
        Assert.assertEquals(0, LinesReachCalculator.calculateLinesToReach(getMockConfig(0, 0), getMockLicense(10, 9, 1)));
        Assert.assertEquals(1, LinesReachCalculator.calculateLinesToReach(getMockConfig(2, 0), getMockLicense(10, 9, 1)));
        Assert.assertEquals(2, LinesReachCalculator.calculateLinesToReach(getMockConfig(2, 1), getMockLicense(10, 9, 1)));
        Assert.assertEquals(10, LinesReachCalculator.calculateLinesToReach(getMockConfig(0, 10), getMockLicense(10, 10, 0)));
        Assert.assertEquals(10, LinesReachCalculator.calculateLinesToReach(getMockConfig(0, 20), getMockLicense(10, 10, 0)));
        Assert.assertEquals(10, LinesReachCalculator.calculateLinesToReach(getMockConfig(10, 0), getMockLicense(10, 10, 1)));
        Assert.assertEquals(10, LinesReachCalculator.calculateLinesToReach(getMockConfig(20, 0), getMockLicense(10, 10, 1)));
        Assert.assertEquals(0, LinesReachCalculator.calculateLinesToReach(getMockConfig(5, 0), getMockLicense(10, 5, 1)));
        Assert.assertEquals(1, LinesReachCalculator.calculateLinesToReach(getMockConfig(5, 1), getMockLicense(10, 5, 1)));

        Assert.assertEquals(0, LinesReachCalculator.calculateLinesToReach(getMockConfig(2, 400000), getMockLicense(50000000, 49000000, 300000)));
        Assert.assertEquals(100000, LinesReachCalculator.calculateLinesToReach(getMockConfig(2, 500000), getMockLicense(50000000, 49000000, 300000)));
    }

    private static Config getMockConfig(int thresholdCoeff, long numberLocAdd) {
        Config config = mock(Config.class);
        when(config.getThresholdCoeff()).thenReturn(thresholdCoeff);
        when(config.getNumberLocAdd()).thenReturn(numberLocAdd);
        return config;
    }

    private static License getMockLicense(long maxLoc, long loc, long remainingLocThreshold) {
        License license = mock(License.class);
        when(license.getMaxLoc()).thenReturn(maxLoc);
        when(license.getLoc()).thenReturn(loc);
        when(license.getRemainingLocThreshold()).thenReturn(remainingLocThreshold);
        return license;
    }
}
