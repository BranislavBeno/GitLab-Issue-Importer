plugins {
    java
    application
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(20))
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
    implementation("software.amazon.awscdk:aws-cdk-lib:2.139.0")
}

version = "0.1.0-SNAPSHOT"
