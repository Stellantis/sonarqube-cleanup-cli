
# SonarQube old projects Cleanup command line client

This is a java cli program to remove old [SonarQube](https://www.sonarqube.org/) projects, to keep the total number of lines of code below that allowed by the license.

## Usage

*Prerequisite: [java](https://www.java.com/fr/download/) should be installed and on your PATH.*

Execute `java -jar sonarqube-cleanup-cli-x.y.jar -h http://sonarqube.company.com -l adminUser -p foobar`

Options: 

```
usage: sonarqube-cleanup-cli
 -d,--dryRun                 Dry run mode, no projects will be deleted
                             (default: false).
 -h,--hostUrl <arg>          The SonarQube server URL.
 -l,--login <arg>            The login or authentication token of a
                             SonarQube user with admin permission.
 -n,--numberLocAdd <arg>     The number of LoC in addition to retrieve
                             (default: 0).
 -p,--password <arg>         The password that goes with the login
                             username. This should be left blank if an
                             authentication token is being used.
 -t,--thresholdCoeff <arg>   The multiplicative coefficient on threshold
                             to calculate LoC to retrieve (default: 2).
 -y,--yes                    Assume projects deletion without confirmation
                             (default: false).
```

## Explanation about Line Of Code calculation for deletion

The goal of this program is to delete old SonarQube projects, before the total lines of code authorized by license is reached.

When the [threshold](https://docs.sonarqube.org/display/PLUG/License+Manager+Plugin) is reached (~6% of total LoC by default), a email is sent to administrators.

Two options can be configured to customize behavior, depends the needs ; mainly manual or batch/cron/daily execution.

- **thresholdCoeff** : This is the threshold multiplicator coefficient, to calculate the number of lines of code to retrieve in projects deletion ; useful when you want execute this program manually on SonarQube email reception
- **numberLocAdd** : This is the number of line to retrieve in projects deletion, in addition of threshold multiplicator coefficient ; useful when you want execute this program by cron


The combination of both could be done. Think to configure the threshold in `http://sonarqube.company.com/admin/extension/license/app` with your needs. 

**Sample**: Your license is 100 millions LoC and you want 1 million of limit (for new big project), but keep the email if limit prior to thousand (in case of problem, if cron program is not correctly executed):

- Configure your SonarQube threshold to 100000.
- Configure *numberLocAdd* option to 800000 and let *thresholdCoeff* to 2 (default value): `100000 x 2 + 800000 = 1000000`


## Prerequisite

SonarQube **v6.2**, due to [api/components/search_projects](https://sonarcloud.io/web_api/api/components/search_projects?internal=true) usage.


