package org.psa.sonarqube.cleanup.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

public class Config {

    public static final int USER_TOKEN_LENGTH_MIN = 40;

    private static final String OPTION_HOST_URL = "h";
    private static final String OPTION_LOGIN = "l";
    private static final String OPTION_PASSWORD = "p"; // NOSONAR : Password should be stored
    private static final String OPTION_DRY_RUN = "d";
    private static final String OPTION_YES = "y";
    private static final String OPTION_THRESHOLD_COEFF = "t";
    private static final String OPTION_NUMBER_LOC_ADD = "n";

    private static final String SYSENV_HOST_URL = "SONAR_HOST_URL";
    private static final String SYSENV_LOGIN = "SONAR_LOGIN";
    private static final String SYSENV_PASSWORD = "SONAR_PASSWORD"; // NOSONAR : Key reference
    private static final String SYSENV_DRY_RUN = "SONAR_CLEANUP_DRY_RUN";
    private static final String SYSENV_YES = "SONAR_CLEANUP_YES";
    private static final String SYSENV_THRESHOLD_COEFF = "SONAR_CLEANUP_THRESHOLD_COEFF";
    private static final String SYSENV_NUMBER_LOC_ADD = "SONAR_CLEANUP_NUMBER_LOC_ADD";

    private static final String DESC_HOST_URL = "The SonarQube server URL (or sysenv '%s').";
    private static final String DESC_LOGIN = "The login or authentication token of a SonarQube user with admin permission (or sysenv '%s').";
    private static final String DESC_PASSWORD = "The password that goes with the login username. This should be left blank if an authentication token is being used (or sysenv '%s')."; // NOSONAR
    private static final String DESC_DRY_RUN = "Dry run mode, no projects will be deleted (default: false ; or sysenv '%s').";
    private static final String DESC_YES = "Assume projects deletion without confirmation (default: false ; or sysenv '%s').";
    private static final String DESC_THRESHOLD_COEFF = "The multiplicative coefficient on threshold to calculate LoC to retrieve (default: 2 ; or sysenv '%s').";
    private static final String DESC_NUMBER_LOC_ADD = "The number of LoC in addition to retrieve (default: 0 ; or sysenv '%s').";

    private String hostUrl;
    private String login;
    private String password;
    private boolean dryRun = false;
    private boolean yes = false;
    private int thresholdCoeff = 2;
    private long numberLocAdd = 0;

    public Config(String[] args) throws ParseException {
        CommandLine cmd = parseCmd(populateWithSysEnv(args));
        this.hostUrl = cmd.getOptionValue(OPTION_HOST_URL);
        this.login = cmd.getOptionValue(OPTION_LOGIN);
        this.password = cmd.getOptionValue(OPTION_PASSWORD);
        this.dryRun = cmd.hasOption(OPTION_DRY_RUN);
        this.yes = cmd.hasOption(OPTION_YES);
        if (cmd.hasOption(OPTION_THRESHOLD_COEFF)) {
            this.thresholdCoeff = Integer.parseInt(cmd.getOptionValue(OPTION_THRESHOLD_COEFF));
        }
        if (cmd.hasOption(OPTION_NUMBER_LOC_ADD)) {
            this.numberLocAdd = Long.parseLong(cmd.getOptionValue(OPTION_NUMBER_LOC_ADD));
        }

        // Minimal checks in addition of parsing
        if (isLoginUserToken() && StringUtils.isNotBlank(getPassword())) {
            throw new UnsupportedOperationException("User token is used for connection and password is not empty, invalid configuration");
        }
    }

    private static CommandLine parseCmd(String[] args) throws ParseException {
        Options options = new Options();
        options.addRequiredOption(OPTION_HOST_URL, "hostUrl", true, String.format(DESC_HOST_URL, SYSENV_HOST_URL));
        options.addRequiredOption(OPTION_LOGIN, "login", true, String.format(DESC_LOGIN, SYSENV_LOGIN));
        options.addOption(OPTION_PASSWORD, "password", true, String.format(DESC_PASSWORD, SYSENV_PASSWORD));
        options.addOption(OPTION_DRY_RUN, "dryRun", false, String.format(DESC_DRY_RUN, SYSENV_DRY_RUN));
        options.addOption(OPTION_YES, "yes", false, String.format(DESC_YES, SYSENV_YES));
        options.addOption(OPTION_THRESHOLD_COEFF, "thresholdCoeff", true, String.format(DESC_THRESHOLD_COEFF, SYSENV_THRESHOLD_COEFF));
        options.addOption(OPTION_NUMBER_LOC_ADD, "numberLocAdd", true, String.format(DESC_NUMBER_LOC_ADD, SYSENV_NUMBER_LOC_ADD));
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.setOptionComparator(null);
            formatter.setWidth(200);
            formatter.printHelp("sonarqube-cleanup-cli", options);
            throw e;
        }
    }

    private static String[] populateWithSysEnv(String[] args) {
        args = parseSysEnvOverride(args, SYSENV_HOST_URL, OPTION_HOST_URL);
        args = parseSysEnvOverride(args, SYSENV_LOGIN, OPTION_LOGIN);
        args = parseSysEnvOverride(args, SYSENV_PASSWORD, OPTION_PASSWORD);
        args = parseSysEnvOverride(args, SYSENV_DRY_RUN, OPTION_DRY_RUN);
        args = parseSysEnvOverride(args, SYSENV_YES, OPTION_YES);
        args = parseSysEnvOverride(args, SYSENV_THRESHOLD_COEFF, OPTION_THRESHOLD_COEFF);
        args = parseSysEnvOverride(args, SYSENV_NUMBER_LOC_ADD, OPTION_NUMBER_LOC_ADD);
        return args;
    }

    private static String[] parseSysEnvOverride(String[] args, String sysEnv, String optionName) {
        String value = System.getenv(sysEnv);
        if (StringUtils.isBlank(value)) {
            return args;
        }
        List<String> l = new ArrayList<>();
        l.add("-" + optionName);
        l.add(value);
        l.addAll(Arrays.asList(args));
        return l.toArray(new String[l.size()]);
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public String getLogin() {
        return login;
    }

    public String getLoginForDisplay() {
        if (isLoginUserToken()) {
            return "[tokenHidden]";
        }
        return getLogin();
    }

    public boolean isLoginUserToken() {
        return getLogin().length() >= USER_TOKEN_LENGTH_MIN;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isDryRun() {
        return dryRun;
    }

    public boolean isYes() {
        return yes;
    }

    public int getThresholdCoeff() {
        return thresholdCoeff;
    }

    public long getNumberLocAdd() {
        return numberLocAdd;
    }
}
