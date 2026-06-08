plugins {
    java
    id("com.github.spotbugs") version "6.0.18"
    id("io.github.goooler.shadow") version "8.1.8"
}

group = "dev.marriage"
version = "v1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    // Paper API — provided by server, do NOT shade
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")

    // Vault Economy API — provided by server, do NOT shade
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")

    // SQLite JDBC — shaded into the jar
    implementation("org.xerial:sqlite-jdbc:3.47.1.0")

    // SpotBugs annotations for @SuppressFBWarnings
    compileOnly("com.github.spotbugs:spotbugs-annotations:4.8.6")

    // Test dependencies
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0")
    testImplementation("org.mockito:mockito-core:5.12.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.12.0")
}

tasks {
    // Expand ${version} in plugin.yml
    processResources {
        val props = mapOf("version" to project.version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }

    compileJava {
        options.encoding = "UTF-8"
        options.release.set(21)
        // Enable most warnings; suppress deprecation so they don't clutter output
        // (Paper 1.21 deprecates ChatColor and getDescription() — handled in code)
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing", "-Xlint:-deprecation"))
    }

    // Disable the default thin jar
    jar {
        enabled = false
    }

    // Fat jar with relocated SQLite to avoid class conflicts
    shadowJar {
        archiveClassifier.set("")
        // Relocate SQLite to avoid conflict with other plugins
        relocate("org.sqlite", "dev.marriage.libs.sqlite")
        // Keep only what we need
        minimize()
    }

    // Build always produces the shadow jar
    build {
        dependsOn(shadowJar)
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

// ── SpotBugs Configuration ──────────────────────────────────────────────────
spotbugs {
    toolVersion.set("4.8.6")
    effort.set(com.github.spotbugs.snom.Effort.MAX)
    reportLevel.set(com.github.spotbugs.snom.Confidence.LOW)
    excludeFilter.set(file("spotbugs-exclude.xml"))
    // Do NOT fail the build on bugs — only generate the report during verify
    ignoreFailures.set(true)
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask>().configureEach {
    reports {
        create("html") {
            required.set(true)
        }
        create("xml") {
            required.set(false)
        }
    }
}
