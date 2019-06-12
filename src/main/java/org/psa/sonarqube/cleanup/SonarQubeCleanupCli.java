package org.psa.sonarqube.cleanup;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.psa.sonarqube.cleanup.config.Config;
import org.psa.sonarqube.cleanup.rest.SonarQubeClient;
import org.psa.sonarqube.cleanup.rest.model.Component;
import org.psa.sonarqube.cleanup.rest.model.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SonarQubeCleanupCli {

    private static final Logger LOG = LoggerFactory.getLogger(SonarQubeCleanupCli.class);
    private static final Object LS = System.lineSeparator();

    SonarQubeCleanupCli() {
        super();
    }

    public static void main(String[] args) {
        Config config = null;
        try {
            config = new Config(args);
        } catch (ParseException e) {
            // When error, 'help' has been display, so error in 1 line and exit
            LOG.error("{}Error in parsing command line ; {}{}", LS, e.getMessage(), LS);
            return;
        }
        LOG.info("Connecting to        : {} (user: {})", config.getHostUrl(), config.getLoginForDisplay());
        completeConfigWithInputPassword(config);
        SonarQubeClient client = SonarQubeClient.build(config);
        License license = client.getLicence();
        LOG.info("License information  : maxLoc={} / loc={} / threshold={}", license.getMaxLoc(), license.getLoc(),
                license.getRemainingLocThreshold());
        long nLoc2Reach = LinesReachCalculator.calculateLinesToReach(config, license);
        LOG.info("{}Number line to reach : {} (calculated with thresholdCoeff={}, numberLinesToAdd={})", LS, nLoc2Reach, config.getThresholdCoeff(),
                config.getNumberLocAdd());
        if (nLoc2Reach == 0) {
            LOG.info("No LoC to reach, exit.");
            return;
        }

        // Iterate on projects to delete
        LOG.info("{}Iterate on projects (loc / key / name) to delete until reaching {} lines ...", LS, nLoc2Reach);
        List<String> projectKeys = new ArrayList<>();
        for (Component p : client.getProjectsOldMax500().getComponents()) {
            Component pd = client.getProject(p.getKey());
            projectKeys.add(p.getKey());
            LOG.info("{} / {} / {}", pd.getNcloc(), p.getKey(), p.getName());
            nLoc2Reach = nLoc2Reach - pd.getNcloc();
            if (nLoc2Reach <= 0) {
                break;
            }
        }

        if (config.isDryRun()) {
            LOG.info("Dry-run mode, no deletion, exit.");
            return;
        }

        LOG.info("{}{} project(s) will be deleted ...", LS, projectKeys.size());
        if (!config.isYes()) {
            LOG.info("Please confirm: [y/Y]");
            if (!"y".equalsIgnoreCase(readLine())) {
                LOG.info("Deletion aborted, exit.");
                return;
            }
        }
        for (String k : projectKeys) {
            LOG.info("Deleting project: {}", k);
            client.deleteProject(k);
        }
        LOG.info("Deletion done, exit.");
    }

    private static void completeConfigWithInputPassword(Config config) {
        if (config.isLoginUserToken() || StringUtils.isNoneBlank(config.getPassword())) {
            return;
        }
        LOG.info("SonarQube password ? : ");
        config.setPassword(String.valueOf(readLine()));
    }

    @SuppressWarnings("resource")
    private static String readLine() {
        // Using System.console would be better, but no solution to mock for UT
        return new Scanner(System.in).nextLine();
    }

}
