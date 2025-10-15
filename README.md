[![Application Tests](https://github.com/BranislavBeno/GitLab-Issue-Importer/actions/workflows/03-run-tests.yml/badge.svg)](https://github.com/BranislavBeno/GitLab-Issue-Importer/actions/workflows/03-run-tests.yml)
[![Application Deployment](https://github.com/BranislavBeno/GitLab-Issue-Importer/actions/workflows/04-build-and-deploy-application.yml/badge.svg)](https://github.com/BranislavBeno/GitLab-Issue-Importer/actions/workflows/04-build-and-deploy-application.yml)  
[![Security Rating](https://sonarcloud.io/api/project_badges/measure?project=BranislavBeno_GitlabIssueImporter&metric=security_rating)](https://sonarcloud.io/summary/new_code?id=BranislavBeno_GitlabIssueImporter)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=BranislavBeno_GitlabIssueImporter&metric=coverage)](https://sonarcloud.io/summary/new_code?id=BranislavBeno_GitlabIssueImporter)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=BranislavBeno_GitlabIssueImporter&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=BranislavBeno_GitlabIssueImporter)  
[![](https://img.shields.io/badge/Java-21-blue)](/app/build.gradle.kts)
[![](https://img.shields.io/badge/Spring%20Boot-3.5.6-blue)](/app/build.gradle.kts)
[![](https://img.shields.io/badge/Testcontainers-2.0.0-blue)](/app/build.gradle.kts)
[![](https://img.shields.io/badge/Gradle-9.1.0-blue)](/gradle/wrapper/gradle-wrapper.properties)
[![](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

## Web application for importing issues into Gitlab

This web application offers to import issues from other ticketing system into Gitlab.  
Currently, is supported *ClearQuest* system and its issues must be provided in form of CSV file.  
However, it's relatively easy to extend application for other ticketing systems.

### Installation
Preferred way of installation is to pull and run prepared docker image `docker pull beo1975/gitlab-issue-importer:1.0.0`.  
Precondition is to have `docker` installed on the hosting OS.

Alternatively is possible to build and run the application as a fat jar on any hosting OS with `Java 21` installed.

Application expects only running instance of Gitlab into which the issues will be imported.
No other services (e.g. databases, message brokers,...) are required.

### Usage
Application is simple web server, which by default listens on port 8080.  
For successful issues import is necessary to set required parameters over web UI.

There are two ways how to proceed:
1. Upload all necessary parameters from prepared `properties` file, e.g.:
``` properties
   project.url=https://gitlab.com
   project.id=31643739
   project.access.token=gitlab-access-token
   csv.type=ClearQuest
   csv.delimiter=;
```

> For access token obtaining, see: [Project access tokens](https://docs.gitlab.com/ee/user/project/settings/project_access_tokens.html).  
> For sake of safety, it's possible to provide `project.access.token` as an environment variable `PROJECT_ACCESS_TOKEN` on hosting OS.

In this case user uploads chosen `properties` file over `Import settings` view.
![](docs/images/uploadProperties.png)

2. Set all necessary parameters manually

In this case user sets up necessary parameters manually over `Upload issues` view.

![](docs/images/uploadIssues.png)

In both cases user must choose file with issues, which are going to be imported.
The result will be shown on `Results` view.
![](docs/images/uploadIssuesResult.png)

### Supported ticketing systems
#### ClearQuest
Input CSV file must contain at least following columns:
- Headline
- CFXID
Otherwise import ends with error.

Following columns from CSV file are also processed:
- Description
- SystemStructure
- CCBNotesLog
- NotesLog
- Attachments

Any other columns are ignored during import.
