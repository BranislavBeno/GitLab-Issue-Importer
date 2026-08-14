# GitLab Issue Importer

Spring Boot web app that imports issues from other ticketing systems (currently CSV exports from IBM ClearQuest)
into GitLab. Multi-module Gradle build with a separate AWS CDK deployment module.

## Modules

- `app/` — the Spring Boot web application (Java 25, Spring Boot 4). This is where almost all feature work happens.
- `cdk/` — AWS CDK (Java) infrastructure-as-code for bootstrapping AWS, an ECR repository, networking, and the
  Fargate service that runs the app. Deployed via npm scripts that shell out to Gradle (see `cdk/package.json`).
  Only touch this when changing AWS infra.
- `docker/` — supporting docker assets. `Dockerfile` at repo root builds a layered Spring Boot image with a
  non-root user, using `azul/zulu-openjdk-alpine:25.0.3`.

## Build, test, and lint

Run all Gradle commands from the repo root using the wrapper (`./gradlew`, or `gradlew.bat` on Windows).

- Build/compile app: `./gradlew :app:build`
- Run all app tests: `./gradlew :app:test`
- Run a single test class: `./gradlew :app:test --tests "com.issue.importer.service.IssueTrackingServiceTest"`
- Run a single test method: `./gradlew :app:test --tests "com.issue.importer.service.IssueTrackingServiceTest.methodName"`
- Coverage report (also used in CI): `./gradlew :app:jacocoTestReport`
- Code formatting/migration recipes (OpenRewrite, dry-run): `./gradlew :app:rewriteDryRun`; apply with
  `./gradlew :app:rewriteRun`. Custom project recipe `com.issue.importer.NoStaticImport` (in `app/rewrite.yml`)
  forbids static imports of Mockito, WireMock, Selenide, Testcontainers, and JUnit Assertions — always import
  those classes and call methods qualified (e.g. `Mockito.when(...)`, `Assertions.assertEquals(...)`).
- CDK build/tests: `./gradlew :cdk:build`

CI (`.github/workflows/03-run-tests.yml`) runs `./gradlew :app:jacocoTestReport :app:sonar` on every push/PR — keep
changes green against that command.

### Test types in `app/src/test`

- Unit tests (e.g. `service/*Test.java`, `domain/*Test.java`) use plain JUnit 5 + Mockito.
- Controller/UI tests (`controller/*Test.java`) are full end-to-end browser tests: they extend
  `AbstractControllerTest`, boot the app with `@SpringBootTest(webEnvironment = RANDOM_PORT)`, and drive a real
  browser via Selenide/Selenium inside Testcontainers (`@Testcontainers(disabledWithoutDocker = true)`), so they
  require Docker to be running locally and are automatically skipped otherwise.
- `WireMock` is used to stub the GitLab REST API in webclient/service tests instead of hitting a real GitLab
  instance.

## Architecture (app module)

Request flow: `ImportController` → `IssueTrackingService` → (`DataReader` for CSV, `IssueWebClient` for GitLab
REST calls) → back to controller for Thymeleaf view rendering.

- `controller/ImportController` — the only `@Controller`; handles three views/flows: index, upload-properties
  (parses an uploaded `.properties` file into `ApplicationSettings`), and upload-issues (parses settings from
  form fields + an uploaded CSV, then delegates the import).
- `service/IssueTrackingService` — a `record` (not a plain class) that orchestrates: read CSV → fetch existing
  GitLab issues → diff by title to split into "new" (to import) vs "existing" (to ignore, based on matching
  `IssueData.title()`) → import only the new ones → return a `ResultData`. Wraps each stage's exceptions into a
  stage-specific unchecked exception (`CsvReadingException`, `IssueFetchingException`, `IssueImportException`)
  and logs before rethrowing.
- `io/csv/` — CSV parsing (OpenCSV-based `CsvDataReader` implementing `DataReader`), converts rows into
  `IssueData` based on `ApplicationSettings.csvType()`.
- `io/props/` — reads an uploaded `.properties` file into `ApplicationSettings` (`PropsSettingsReader`
  implementing `SettingsReader`).
- `webclient/IssueWebClient` — talks to the GitLab REST API (fetch/import issues) using the project URL, id, and
  access token from `ApplicationSettings`.
- `domain/` — model types: `ApplicationSettings` (url, projectId, accessToken, csv type/delimiter — accessToken
  can also come from the `PROJECT_ACCESS_TOKEN` env var), `CsvType` (enum mapping a label to a `CsvRow` subtype
  and a `forProduction` flag — only types with `forProduction = true`, e.g. `ClearQuest`, are offered in the
  upload UI via `CsvType.FOR_PRODUCTION`), `Issue`/`IssueData`/`CsvRow` implementations per ticketing system
  (currently `ClearQuest`, plus `User` used only in tests).

### Adding support for a new ticketing system

Add a new `CsvRow` implementation (see `ClearQuest`) and register it as a new `CsvType` enum constant with
`forProduction = true`; the CSV reader and upload UI pick it up automatically via `CsvType.FOR_PRODUCTION`.

## Conventions

- Prefer Java `record` for immutable data/services (`IssueTrackingService`, `ApplicationSettings`, `ResultData`
  are records); use classes with getters/setters mainly for Jackson-deserialized DTOs like `Issue`.
- Catch blocks that intentionally discard the exception use the unnamed variable pattern `catch (Exception _)`.
- Views are Thymeleaf templates in `app/src/main/resources/templates`, using the layout dialect
  (`thymeleaf-layout-dialect`).
- Sensitive config (`project.access.token`) should never be hardcoded — it's read from an uploaded properties
  file or the `PROJECT_ACCESS_TOKEN` environment variable.
