
[![Build Status](https://github.com/GroupePSA/sonarqube-cleanup-cli/workflows/Development%20Build/badge.svg)](https://github.com/GroupePSA/sonarqube-cleanup-cli/actions?query=workflow%3A%22Development+Build%22) [![SonarCloud Status](https://sonarcloud.io/api/project_badges/measure?project=org.psa%3Asonarqube-cleanup-cli&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.psa%3Asonarqube-cleanup-cli)&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;[![Download](https://img.shields.io/github/v/release/GroupePSA/sonarqube-cleanup-cli)](https://github.com/GroupePSA/sonarqube-cleanup-cli/releases/latest)

# SonarQube old projects Cleanup command line client

This is a simple java cli program to remove old [SonarQube](https://www.sonarqube.org/) projects, to keep the total number of lines of code below that allowed by the license.

## Usage

*Prerequisite: [java](https://www.java.com/fr/download/) should be installed and on your PATH.*

Download `sonarqube-cleanup-cli-x.y.zip` ZIP file from [releases](https://github.com/GroupePSA/sonarqube-cleanup-cli/releases) (or SNAPSHOT from [packages](https://github.com/orgs/GroupePSA/packages?repo_name=sonarqube-cleanup-cli)) and extract it.

Depending your environment system, run `sonarqube-cleanup-cli.bat` (Windows) or `sonarqube-cleanup-cli.sh` (Linux) with options.

At min `sonarqube-cleanup-cli.[sh|bat] -h http://sonarqube.company.com -l adminUser -p foobar`

Options: 

```
usage: sonarqube-cleanup-cli
 -h,--hostUrl <arg>          The SonarQube server URL (or sysenv 'SONAR_HOST_URL').
 -l,--login <arg>            The login or authentication token of a SonarQube user with admin permission (or sysenv 'SONAR_LOGIN').
 -p,--password <arg>         The password that goes with the login username. This should be left blank if an authentication token is being used (or sysenv 'SONAR_PASSWORD').
 
 -d,--dryRun                 Dry run mode, no projects will be deleted (default: false ; or sysenv 'SONAR_CLEANUP_DRY_RUN').
 -y,--yes                    Assume projects deletion without confirmation (default: false ; or sysenv 'SONAR_CLEANUP_YES').
 
 -t,--thresholdCoeff <arg>   The multiplicative coefficient on threshold to calculate LoC to retrieve (default: 2 ; or sysenv 'SONAR_CLEANUP_THRESHOLD_COEFF').
 -n,--numberLocAdd <arg>     The number of LoC in addition to retrieve (default: 0 ; or sysenv 'SONAR_CLEANUP_NUMBER_LOC_ADD').
```

## Explanation about Line Of Code calculation for deletion

The goal of this program is to delete old SonarQube projects, before the total lines of code authorized by license is reached.

When the [threshold](https://docs.sonarqube.org/display/PLUG/License+Manager+Plugin) is reached (~6% of total LoC by default), a email is sent to administrators.

Two options can be configured to customize behavior, depends the needs ; mainly manual or batch/cron/daily execution.

- **thresholdCoeff** : This is the threshold multiplicator coefficient, to calculate the number of lines of code to retrieve in projects deletion ; useful when you want execute this program manually on SonarQube email reception
- **numberLocAdd** : This is the number of line to retrieve in projects deletion, in addition of threshold multiplicator coefficient ; useful when you want execute this program by cron


The combination of both could be done. Think to configure the threshold in `http://sonarqube.company.com/admin/extension/license/app` with your needs. 

**Sample**: Your license is 100 millions LoC and you want 1 million of limit (for new big project), but keep the email if limit prior to one hundred thousand (in case of problem, if cron program is not correctly executed):

- Configure your SonarQube threshold to 100000.
- Configure *numberLocAdd* option to 800000 and let *thresholdCoeff* to 2 (default value): `100000 x 2 + 800000 = 1000000`


## Prerequisite

SonarQube **v6.2**, due to [api/components/search_projects](https://sonarcloud.io/web_api/api/components/search_projects?internal=true) usage.

## Development process

This project is using [Maven](https://maven.apache.org/) as integration tool.

For development/SNAPSHOT build, use:

```
mvn package
```

For release build, use (GitHub credentials specially required on command line if [2FA](https://help.github.com/en/articles/configuring-two-factor-authentication) used):

```
git reset --hard origin/master 
git branch -m next-version 
mvn -B clean release:clean release:prepare -Dusername=yourGitHubLogin -Dpassword=yourGitHubToken
```

After that, you would have to create pull-request from `next-version` branch and rebase it on master for next version development.
