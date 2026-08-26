import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    application
    jacoco
    id("org.springframework.boot") version "4.1.1"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.sonarqube") version "7.4.0.8496"
    id("com.gorylenko.gradle-git-properties") version "4.0.1"
    id("org.cyclonedx.bom") version "3.4.1"
    id("org.openrewrite.rewrite") version "7.39.0"
}

jacoco {
    toolVersion = "0.8.15"
}

sonarqube {
    properties {
        property("sonar.projectKey", "BranislavBeno_GitlabIssueImporter")
        property("sonar.projectName", "gitlab-issue-importer")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
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
    val thymeleafLayoutVersion = "4.0.1"
    val micrometerPrometheusVersion = "1.17.1"
    val openCsvVersion = "5.12.0"
    val commonsCodecVersion = "1.22.1"
    val snakeYamlVersion = "2.6"
    val selenideVersion = "7.18.0"
    val wireMockVersion = "3.13.2"
    val testcontainersBomVersion = "2.0.5"
    val rewriteRecipeBomVersion = "3.37.0"

    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:${thymeleafLayoutVersion}")
    implementation("io.micrometer:micrometer-registry-prometheus:${micrometerPrometheusVersion}")
    implementation("com.opencsv:opencsv:${openCsvVersion}")
    implementation("commons-codec:commons-codec:${commonsCodecVersion}")
    implementation("org.yaml:snakeyaml:${snakeYamlVersion}")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.codeborne:selenide:${selenideVersion}")
    testImplementation("org.wiremock:wiremock:${wireMockVersion}")
    testImplementation(platform("org.testcontainers:testcontainers-bom:${testcontainersBomVersion}"))
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-selenium")

    rewrite(platform("org.openrewrite.recipe:rewrite-recipe-bom:${rewriteRecipeBomVersion}"))
    rewrite("org.openrewrite.recipe:rewrite-migrate-java")
    rewrite("org.openrewrite.recipe:rewrite-spring")
}

rewrite {
    activeRecipe("com.issue.importer.NoStaticImport")
    activeRecipe("org.openrewrite.java.RemoveUnusedImports")
    activeRecipe("org.openrewrite.java.OrderImports")
    activeRecipe("org.openrewrite.java.migrate.UpgradeToJava25")
    activeRecipe("org.openrewrite.java.spring.boot4.SpringBootProperties_4_0")
    activeRecipe("org.openrewrite.java.spring.boot4.UpgradeSpringBoot_4_0")
}

gitProperties { dotGitDirectory.set(File("${project.rootDir}/.git")) }

val versionMajor = 1
val versionMinor = 0
val versionPatch = 0
version = "R$versionMajor.$versionMinor.$versionPatch"

val testSummaryListener = object : TestListener {
    override fun beforeSuite(suite: TestDescriptor) = Unit

    override fun afterSuite(suite: TestDescriptor, result: TestResult) {
        if (suite.parent == null) {
            logger.lifecycle("\nTest result: ${result.resultType}")
            logger.lifecycle(
                "Test summary: " +
                        "${result.testCount} tests, " +
                        "${result.successfulTestCount} succeeded, " +
                        "${result.failedTestCount} failed, " +
                        "${result.skippedTestCount} skipped"
            )
        }
    }

    override fun beforeTest(testDescriptor: TestDescriptor) = Unit

    override fun afterTest(testDescriptor: TestDescriptor, result: TestResult) = Unit
}


tasks.getByName<BootJar>("bootJar") {
    this.archiveFileName.set("gitlab-issue-importer.jar")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    addTestListener(testSummaryListener)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}
