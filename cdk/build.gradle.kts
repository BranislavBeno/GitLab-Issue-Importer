plugins {
    java
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(19))
        vendor.set(JvmVendorSpec.AZUL)
    }
}

application {
    mainClass.set(
            if (project.hasProperty("mainClass"))
                project.properties["mainClass"].toString()
            else "Main class not defined!"
    )
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("software.amazon.awscdk:aws-cdk-lib:2.73.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
}

version = "0.1.0-SNAPSHOT"

tasks.test {
    useJUnitPlatform()
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
