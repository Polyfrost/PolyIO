@file:Suppress("VulnerableLibrariesLocal")

plugins {
    `java-library`
    id("io.freefair.lombok") version "6.6.1"
    `maven-publish`
    signing
}

group = "cc.polyfrost"
version = "0.0.13"
description = "A library for file and download handling."
val gitHost = "github.com"
val repoId = "polyfrost/PolyIO"

repositories {
    maven("https://repo.polyfrost.cc/releases")
    mavenCentral()
}

dependencies {
//    implementation("fr.stardustenterprises:plat4k:1.6.3")
    implementation("org.jetbrains:annotations:24.0.1")
    @Suppress("GradlePackageUpdate")
    implementation("org.apache.logging.log4j:log4j-api:2.0-beta9")
    @Suppress("GradlePackageUpdate")
    testRuntimeOnly(runtimeOnly("org.apache.logging.log4j:log4j-core:2.0-beta9")!!)

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")
}

java {
    withJavadocJar()
    withSourcesJar()

    toolchain {
        languageVersion.set(JavaLanguageVersion.of(8))
    }
}

val apiJar by tasks.creating(Jar::class) {
    archiveClassifier.set("api")
    from(sourceSets["main"].output)
    include("**/api/*")
    group = "build"
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
            "Bundle-DocURL" to "https://$gitHost/$repoId",
            "Bundle-Version" to project.version,
            "Bundle-Description" to description,
        )
        from("LICENSE")
    }

    getByName<Test>("test") {
        useJUnitPlatform()
    }
}

artifacts {
    archives(apiJar)
}

publishing {
    publications {
        // Sets up the Maven integration.
        create("PolyIO", MavenPublication::class.java) {
            from(components["java"])
            artifact(apiJar)

            pom {
                name.set(project.name)
                description.set(project.description)
                url.set("https://$gitHost/$repoId")

                licenses {
                    license {
                        name.set("ISC License")
                        url.set("https://opensource.org/licenses/ISC")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set("xtrm")
                        email.set("oss@xtrm.me")
                    }
                }

                scm {
                    connection.set("scm:git:git://$gitHost/$repoId.git")
                    developerConnection.set("scm:git:ssh://$gitHost/$repoId.git")
                    url.set("https://$gitHost/$repoId")
                }
            }

            // Configure the signing extension to sign this Maven artifact.
            signing {
                isRequired = project.properties["signing.keyId"] != null
                sign(this@create)
            }
        }
    }

    repositories {
        mavenLocal()
        maven {
            url = uri("https://repo.polyfrost.cc/releases")
            name = "polyReleases"
            credentials(PasswordCredentials::class)
        }
        maven {
            url = uri("https://repo.polyfrost.cc/snapshots")
            name = "polySnapshots"
            credentials(PasswordCredentials::class)
        }
        maven {
            url = uri("https://repo.polyfrost.cc/private")
            name = "polyPrivate"
            credentials(PasswordCredentials::class)
        }
    }
}
