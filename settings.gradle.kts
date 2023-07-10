plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.6.0"
}

rootProject.name = "gitlab-issue-importer"

include("app")
include("cdk")