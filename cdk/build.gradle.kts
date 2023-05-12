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
    implementation("software.amazon.awscdk:aws-cdk-lib:2.79.1")
}

version = "0.1.0-SNAPSHOT"
