plugins {
    java
    id("io.freefair.lombok") version "6.6.1"
}

group = "cc.polyfrost"
version = "0.0.1"

repositories {
    maven("https://repo.polyfrost.cc/releases")
}

dependencies {
    implementation("fr.stardustenterprises:plat4k:1.6.3")
    implementation("org.jetbrains:annotations:24.0.0")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}