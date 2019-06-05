package org.psa.sonarqube.cleanup.config;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Config {

    private static final String OPTION_HOST_URL = "h";
    private static final String OPTION_LOGIN = "l";
    private static final String OPTION_PASSWORD = "p"; // NOSONAR : Password should be stored
    private static final String OPTION_DRY_RUN = "d";
    private static final String OPTION_YES = "y";
    private static final String OPTION_THRESHOLD_COEFF = "t";
    private static final String OPTION_NUMBER_LOC_ADD = "n";

    private String hostUrl;
    private String login;
    private String password;
    private boolean dryRun = false;
    private boolean yes = false;
    private int thresholdCoeff = 2;
    private long numberLocAdd = 0;

    public Config(String[] args) {
        CommandLine cmd = parseCmd(args);
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
    }

    private static CommandLine parseCmd(String[] args) {
        Options options = new Options();
        options.addRequiredOption(OPTION_HOST_URL, "hostUrl", true, "The SonarQube server URL.");
        options.addRequiredOption(OPTION_LOGIN, "login", true, "The login or authentication token of a SonarQube user with admin permission.");
        options.addOption(OPTION_PASSWORD, "password", true,
                "The password that goes with the login username. This should be left blank if an authentication token is being used.");
        options.addOption(OPTION_DRY_RUN, "dryRun", false, "Dry run mode, no projects will be deleted (default: false).");
        options.addOption(OPTION_YES, "yes", false, "Assume projects deletion without confirmation (default: false).");
        options.addOption(OPTION_THRESHOLD_COEFF, "thresholdCoeff", true,
                "The multiplicative coefficient on threshold to calculate LoC to retrieve (default: 2).");
        options.addOption(OPTION_NUMBER_LOC_ADD, "numberLocAdd", true, "The number of LoC in addition to retrieve (default: 0).");
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("sonarqube-cleanup-cli", options);
            throw new UnsupportedOperationException(e);
        }
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
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
