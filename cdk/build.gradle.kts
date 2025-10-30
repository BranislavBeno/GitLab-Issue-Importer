plugins {
    java
    application
    id("com.gorylenko.gradle-git-properties") version "2.5.3"
    id("org.openrewrite.rewrite") version "7.19.0"
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
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
    implementation("software.amazon.awscdk:aws-cdk-lib:2.221.1")

    rewrite(platform("org.openrewrite.recipe:rewrite-recipe-bom:3.17.0"))
    rewrite("org.openrewrite.recipe:rewrite-migrate-java")
}

rewrite {
    activeRecipe("org.openrewrite.java.RemoveUnusedImports")
    activeRecipe("org.openrewrite.java.OrderImports")
    activeRecipe("org.openrewrite.java.migrate.UpgradeToJava25")
}

gitProperties { dotGitDirectory.set(File("${project.rootDir}/.git")) }

version = "0.1.0-SNAPSHOT"
