package org.psa.sonarqube.cleanup;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.psa.sonarqube.cleanup.config.Config;
import org.psa.sonarqube.cleanup.rest.SonarQubeClient;
import org.psa.sonarqube.cleanup.rest.model.Component;
import org.psa.sonarqube.cleanup.rest.model.License;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SonarQubeCleanupCli {

    private static final Logger LOG = LoggerFactory.getLogger(SonarQubeCleanupCli.class);

    SonarQubeCleanupCli() {
        super();
    }

    public static void main(String[] args) {
        Config config = new Config(args);
        LOG.info("Connecting to        : {} (user: {})", config.getHostUrl(), config.getLoginForDisplay());
        completeConfigWithInputPassword(config);
        SonarQubeClient client = SonarQubeClient.build(config);
        License license = client.getLicence();
        LOG.info("License information  : maxLoc={} / loc={} / threshold={}", license.getMaxLoc(), license.getLoc(),
                license.getRemainingLocThreshold());
        long nLoc2Reach = LinesReachCalculator.calculateLinesToReach(config, license);
        LOG.info("\nNumber line to reach : {} (calculated with thresholdCoeff={}, numberLinesToAdd={})", nLoc2Reach, config.getThresholdCoeff(),
                config.getNumberLocAdd());
        if (nLoc2Reach == 0) {
            LOG.info("No LoC to reach, exit.");
            return;
        }

        // Iterate on projects to delete
        LOG.info("\nIterate on projects (loc / key / name) to delete until reaching {} lines ...", nLoc2Reach);
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

        LOG.info("\n{} project(s) will be deleted ...", projectKeys.size());
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
