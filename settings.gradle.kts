plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.9.0"
}

rootProject.name = "gitlab-issue-importer"

include("app")
include("cdk")