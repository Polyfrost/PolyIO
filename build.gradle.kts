plugins {
    java
    val dgtVersion = "2.6.0"
    id("dev.deftu.gradle.tools") version(dgtVersion)
    id("dev.deftu.gradle.tools.bloom") version(dgtVersion)
    id("dev.deftu.gradle.tools.publishing.maven") version(dgtVersion)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains:annotations:24.0.1")

    @Suppress("GradlePackageUpdate")
    implementation("org.apache.logging.log4j:log4j-api:2.0-beta9")
    @Suppress("GradlePackageUpdate")
    testRuntimeOnly(runtimeOnly("org.apache.logging.log4j:log4j-core:2.0-beta9")!!)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

tasks {
    test {
        useJUnitPlatform()
    }
}
