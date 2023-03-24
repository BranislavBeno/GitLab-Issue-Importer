import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    java
    application
    jacoco
    id("org.springframework.boot") version "3.0.5"
    id("org.sonarqube") version "4.0.0.2929"
    id("com.gorylenko.gradle-git-properties") version "2.4.1"
    id("org.cyclonedx.bom") version "1.7.4"
}

apply(plugin = "io.spring.dependency-management")

jacoco {
    toolVersion = "0.8.8"
}

sonarqube {
    properties {
        property("sonar.projectKey", "BranislavBeno_GitlabIssueImporter")
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
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
    implementation("nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect:3.2.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.10.5")
    implementation("com.opencsv:opencsv:5.7.1")
    implementation("commons-codec:commons-codec:1.15")
    implementation("org.yaml:snakeyaml:2.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.17.6"))
    testImplementation("org.testcontainers:junit-jupiter")
    testImplementation("org.testcontainers:selenium")
    testImplementation("com.codeborne:selenide:6.12.4")
    testImplementation("com.github.tomakehurst:wiremock:3.0.0-beta-5")
}

val versionMajor = 1
val versionMinor = 0
val versionPatch = 0
version = "R${versionMajor}.${versionMinor}.${versionPatch}"

tasks.getByName<BootJar>("bootJar") {
    this.archiveFileName.set("gitlab-issue-importer.jar")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
    afterSuite(KotlinClosure2<TestDescriptor, TestResult, Unit>({ descriptor, result ->
        if (descriptor.parent == null) {
            logger.lifecycle(
                "\nTest result: ${result.resultType}"
            )
            logger.lifecycle(
                "Test summary: " +
                        "${result.testCount} tests, " +
                        "${result.successfulTestCount} succeeded, " +
                        "${result.failedTestCount} failed, " +
                        "${result.skippedTestCount} skipped"
            )
        }
    }))
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
    }
}

tasks.cyclonedxBom {
    setIncludeConfigs(listOf("runtimeClasspath"))
    setSkipConfigs(listOf("compileClasspath", "testCompileClasspath"))
    setProjectType("application")
    setDestination(project.file("build/reports/sbom"))
    setOutputName("CycloneDX-SBOM")
    setOutputFormat("all")
    setIncludeBomSerialNumber(false)
    setIncludeLicenseText(true)
    setComponentVersion("2.0.0")
}
