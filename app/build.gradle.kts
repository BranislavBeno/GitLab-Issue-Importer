import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    application
    jacoco
    id("org.springframework.boot") version "3.4.2"
    id("org.sonarqube") version "6.0.1.5171"
    id("com.gorylenko.gradle-git-properties") version "2.4.2"
    id("org.cyclonedx.bom") version "2.0.0"
    id("org.openrewrite.rewrite") version "7.0.5"
}

apply(plugin = "io.spring.dependency-management")

jacoco {
    toolVersion = "0.8.12"
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
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.3.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.14.3")
    implementation("com.opencsv:opencsv:5.10")
    implementation("commons-codec:commons-codec:1.18.0")
    implementation("org.yaml:snakeyaml:2.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.codeborne:selenide:7.7.1")
    testImplementation("org.wiremock:wiremock:3.10.0")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.20.4"))
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:selenium")

    rewrite(platform("org.openrewrite.recipe:rewrite-recipe-bom:3.1.0"))
    rewrite("org.openrewrite.recipe:rewrite-migrate-java")
    rewrite("org.openrewrite.recipe:rewrite-spring")
}

rewrite {
    activeRecipe("com.issue.importer.NoStaticImport")
    activeRecipe("org.openrewrite.java.RemoveUnusedImports")
    activeRecipe("org.openrewrite.java.OrderImports")
    activeRecipe("org.openrewrite.java.migrate.UpgradeToJava21")
    activeRecipe("org.openrewrite.java.spring.boot3.SpringBoot3BestPractices")
    activeRecipe("org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_3")
}

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
