pluginManagement {
    repositories {
        maven {
            url = uri("https://artifacts.codegenomeproject.org/maven")
            credentials {
                username = settings.providers.gradleProperty("codegenome.project.user").getOrElse("")
                password = settings.providers.gradleProperty("codegenome.project.token").getOrElse("")
            }
        }
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "gitlab-issue-importer"

include("app")
include("cdk")