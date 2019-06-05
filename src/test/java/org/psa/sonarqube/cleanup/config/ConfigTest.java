package org.psa.sonarqube.cleanup.config;

import org.junit.Assert;
import org.junit.Test;

public class ConfigTest {

    private static final String HOST_LOCAL = "http://localhost";

    @Test
    public void testCmdParserShortMandatoryWithDefaults() {
        Config config = new Config(new String[] { "-h", HOST_LOCAL, "-l", "foo" });
        Assert.assertEquals(HOST_LOCAL, config.getHostUrl());
        Assert.assertEquals("foo", config.getLogin());
        Assert.assertEquals(null, config.getPassword());
        Assert.assertEquals(false, config.isDryRun());
        Assert.assertEquals(false, config.isYes());
        Assert.assertEquals(2, config.getThresholdCoeff());
        Assert.assertEquals(0, config.getNumberLocAdd());
    }

    @Test
    public void testCmdParserLongMandatoryWithDefaults() {
        Config config = new Config(new String[] { "-hostUrl", HOST_LOCAL, "-login", "foo" });
        Assert.assertEquals(HOST_LOCAL, config.getHostUrl());
        Assert.assertEquals("foo", config.getLogin());
        Assert.assertEquals(null, config.getPassword());
        Assert.assertEquals(false, config.isDryRun());
        Assert.assertEquals(false, config.isYes());
        Assert.assertEquals(2, config.getThresholdCoeff());
        Assert.assertEquals(0, config.getNumberLocAdd());
    }

    @Test
    public void testCmdParserShortComplete() {
        Config config = new Config(new String[] { "-h", HOST_LOCAL, "-l", "foo", "-p", "bar", "-d", "-y", "-t", "1", "-n", "2" });
        Assert.assertEquals(HOST_LOCAL, config.getHostUrl());
        Assert.assertEquals("foo", config.getLogin());
        Assert.assertEquals("bar", config.getPassword());
        Assert.assertEquals(true, config.isDryRun());
        Assert.assertEquals(true, config.isYes());
        Assert.assertEquals(1, config.getThresholdCoeff());
        Assert.assertEquals(2, config.getNumberLocAdd());
    }

    @Test
    public void testCmdParserLongComplete() {
        Config config = new Config(new String[] { "-hostUrl", HOST_LOCAL, "-login", "foo", "-password", "bar", "-dryRun", "-yes", "-thresholdCoeff",
                "1", "-numberLocAdd", "2" });
        Assert.assertEquals(HOST_LOCAL, config.getHostUrl());
        Assert.assertEquals("foo", config.getLogin());
        Assert.assertEquals("bar", config.getPassword());
        Assert.assertEquals(true, config.isDryRun());
        Assert.assertEquals(true, config.isYes());
        Assert.assertEquals(1, config.getThresholdCoeff());
        Assert.assertEquals(2, config.getNumberLocAdd());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBadCmdParserBadArg() {
        new Config(new String[] { "-h", HOST_LOCAL, "-l", "foo", "--notExist" });
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBadCmdParserNull() {
        new Config(null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBadCmdParserEmptyArray() {
        new Config(new String[] {});
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testBadCmdParserEmptyStr() {
        new Config(new String[] { "" });
    }

}
