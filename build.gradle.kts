plugins {
    java
    id("io.freefair.lombok") version "6.6.1"
}

group = "cc.polyfrost"
version = "0.0.1"
description = "A library for file and download handling."

repositories {
    maven("https://repo.polyfrost.cc/releases")
}

dependencies {
    implementation("fr.stardustenterprises:plat4k:1.6.3")
    implementation("org.jetbrains:annotations:24.0.0")

    compileOnly("org.apache.logging.log4j:log4j-api:2.14.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    testRuntimeOnly("org.apache.logging.log4j:log4j-core:2.14.1")
}

java {
    withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

tasks {
    jar {
        manifest.attributes += mapOf(
            "Specification-Title" to "polyio",
            "Specification-Vendor" to "polyfrost.cc",
            "Specification-Version" to "1",
            "Implementation-Title" to "polyio",
            "Implementation-Vendor" to "polyfrost.cc",
            "Implementation-Version" to project.version,
            // Java 9
            "Automatic-Module-Name" to "cc.polyfrost.polyio",
            // OSGi
            "Bundle-Name" to "polyio",
            "Bundle-SymbolicName" to "cc.polyfrost.polyio",
            "Bundle-Version" to project.version,
            "Bundle-Description" to description,
        )
    }

    getByName<Test>("test") {
        useJUnitPlatform()
    }
}