plugins {
    java
    application
    id("com.gorylenko.gradle-git-properties") version "2.5.2"
    id("org.openrewrite.rewrite") version "7.12.1"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.AZUL)
    }
}

application {
    mainClass.set(
        if (project.hasProperty("mainClass")) {
            project.properties["mainClass"].toString()
        } else {
            "Main class not defined!"
        },
    )
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.209.1")

    rewrite(platform("org.openrewrite.recipe:rewrite-recipe-bom:3.12.1"))
    rewrite("org.openrewrite.recipe:rewrite-migrate-java")
}

rewrite {
    activeRecipe("org.openrewrite.java.RemoveUnusedImports")
    activeRecipe("org.openrewrite.java.OrderImports")
    activeRecipe("org.openrewrite.java.migrate.UpgradeToJava21")
}

gitProperties { dotGitDirectory.set(File("${project.rootDir}/.git")) }

version = "0.1.0-SNAPSHOT"
