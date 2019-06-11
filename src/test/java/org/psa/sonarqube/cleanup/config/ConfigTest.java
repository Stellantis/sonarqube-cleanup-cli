package org.psa.sonarqube.cleanup.config;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

public class ConfigTest {

    private static final String HOST_LOCAL = "http://localhost";
    private static final String HOST_COMPANY = "http://sonar.company.com";

    @Rule
    public final EnvironmentVariables sysEnv = new EnvironmentVariables();

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
        Assert.assertEquals(config.getLogin(), config.getLoginForDisplay());
        Assert.assertFalse(config.isLoginUserToken());
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
    public void testCmdParserUserToken() {
        Config config = new Config(new String[] { "-h", HOST_LOCAL, "-l", "0000000000000000000000000000000000000000" });
        Assert.assertEquals("[tokenHidden]", config.getLoginForDisplay());
        Assert.assertTrue(config.isLoginUserToken());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testCmdParserUserTokenAndPassword() {
        new Config(new String[] { "-h", HOST_LOCAL, "-l", "0000000000000000000000000000000000000000", "-p", "somePass" });
    }

    @Test
    public void testCmdParserSysEnvComplete() {
        sysEnv.set("SONAR_HOST_URL", HOST_COMPANY);
        sysEnv.set("SONAR_LOGIN", "admin");
        sysEnv.set("SONAR_PASSWORD", "password");
        sysEnv.set("SONAR_CLEANUP_DRY_RUN", "true");
        sysEnv.set("SONAR_CLEANUP_YES", "true");
        sysEnv.set("SONAR_CLEANUP_THRESHOLD_COEFF", "4");
        sysEnv.set("SONAR_CLEANUP_NUMBER_LOC_ADD", "5");

        Config config = new Config(new String[] { "-h", HOST_LOCAL, "-l", "foo", "-p", "bar", "-t", "1", "-n", "2" });
        Assert.assertEquals(HOST_COMPANY, config.getHostUrl());
        Assert.assertEquals("admin", config.getLogin());
        Assert.assertEquals("password", config.getPassword());
        Assert.assertEquals(true, config.isDryRun());
        Assert.assertEquals(true, config.isYes());
        Assert.assertEquals(4, config.getThresholdCoeff());
        Assert.assertEquals(5, config.getNumberLocAdd());
    }

    @Test
    public void testCmdParserSysEnvSomeEmpty() {
        sysEnv.set("SONAR_HOST_URL", "");
        Config config = new Config(new String[] { "-h", HOST_LOCAL, "-l", "foo" });
        Assert.assertEquals(HOST_LOCAL, config.getHostUrl());
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
