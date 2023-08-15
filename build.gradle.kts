plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.8.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("com.diffplug.spotless") version "6.19.0"
}

group = "com.nearvanilla"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    // See line 28.
    // maven("https://oss.sonatype.org/content/repositories/snapshots") // For cloud
    maven {
        name = "papermc-repo"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "sonatype"
        url = uri("https://oss.sonatype.org/content/groups/public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    // Seems to produce a build error at the moment, not sure why.
    // implementation("cloud.commandframework", "cloud-PLATFORM", "1.8.3")
}

val targetJavaVersion = 17

java {
    val javaVersion = JavaVersion.toVersion(targetJavaVersion)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion
    if (JavaVersion.current() < javaVersion) {
        toolchain.languageVersion.set(JavaLanguageVersion.of(targetJavaVersion))
    }
}

tasks.withType(JavaCompile::class).configureEach {
    if (targetJavaVersion >= 10 || JavaVersion.current().isJava10Compatible) {
        options.release.set(targetJavaVersion)
    }
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}

tasks.shadowJar{
    isEnableRelocation = true
    relocationPrefix = "${rootProject.property("group")}.${rootProject.property("name").toString().lowercase()}.lib"
    minimize()
    archiveClassifier.set("")
}

tasks.build{
    dependsOn("shadowJar")
}

spotless {
    format("misc") {
        target(listOf("**/*.gradle", "**/*.md"))
        trimTrailingWhitespace()
        indentWithSpaces(4)
    }
    kotlin {
        ktlint("0.48.2") // 0.49 and 0.50 don't work for some reason.
        licenseHeader("/* Licensed under GNU General Public License v3.0 */")
    }
}