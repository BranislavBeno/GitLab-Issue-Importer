import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    application
    jacoco
    id("org.springframework.boot") version "3.5.6"
    id("org.sonarqube") version "6.3.1.5724"
    id("com.gorylenko.gradle-git-properties") version "2.5.3"
    id("org.cyclonedx.bom") version "2.4.1"
    id("org.openrewrite.rewrite") version "7.18.0"
}

apply(plugin = "io.spring.dependency-management")

jacoco {
    toolVersion = "0.8.14"
}

sonarqube {
    properties {
        property("sonar.projectKey", "BranislavBeno_GitlabIssueImporter")
        property("sonar.projectName", "gitlab-issue-importer")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.AZUL)
    }
}

springBoot {
    buildInfo()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.4.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.15.5")
    implementation("com.opencsv:opencsv:5.12.0")
    implementation("commons-codec:commons-codec:1.19.0")
    implementation("org.yaml:snakeyaml:2.5")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.codeborne:selenide:7.11.1")
    testImplementation("org.wiremock:wiremock:3.13.1")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.21.3"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:selenium")

    rewrite(platform("org.openrewrite.recipe:rewrite-recipe-bom:3.16.0"))
    rewrite("org.openrewrite.recipe:rewrite-migrate-java")
    rewrite("org.openrewrite.recipe:rewrite-spring")
}

rewrite {
    activeRecipe("com.issue.importer.NoStaticImport")
    activeRecipe("org.openrewrite.java.RemoveUnusedImports")
    activeRecipe("org.openrewrite.java.OrderImports")
    activeRecipe("org.openrewrite.java.migrate.UpgradeToJava21")
    activeRecipe("org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_4")
}

gitProperties { dotGitDirectory.set(File("${project.rootDir}/.git")) }

val versionMajor = 1
val versionMinor = 0
val versionPatch = 0
version = "R$versionMajor.$versionMinor.$versionPatch"

tasks.getByName<BootJar>("bootJar") {
    this.archiveFileName.set("gitlab-issue-importer.jar")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    afterSuite(
        KotlinClosure2<TestDescriptor, TestResult, Unit>({ descriptor, result ->
            if (descriptor.parent == null) {
                logger.lifecycle(
                    "\nTest result: ${result.resultType}",
                )
                logger.lifecycle(
                    "Test summary: " +
                            "${result.testCount} tests, " +
                            "${result.successfulTestCount} succeeded, " +
                            "${result.failedTestCount} failed, " +
                            "${result.skippedTestCount} skipped",
                )
            }
        }),
    )
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}
