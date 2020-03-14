import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openjfx.gradle.JavaFXOptions

plugins {
    kotlin("jvm") version "1.3.70"

    id("application")
    id("org.openjfx.javafxplugin") version "0.0.8"
    id("com.novoda.build-properties") version "0.4.1"
}

group = "io.github.rednesto"

buildProperties.create("local") { using(file("local-gradle.properties")) }

repositories {
    mavenCentral()
    maven {
        name = "sponge"
        url = uri("https://repo.spongepowered.org/maven")
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.3.4")
    implementation("org.jetbrains:annotations:19.0.0")
    implementation("org.spongepowered:configurate-xml:3.6.1")
    implementation("org.spongepowered:configurate-hocon:3.6.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "12"
}

application {
    mainClassName = "musicshelf/io.github.rednesto.musicshelf.MusicShelfApp"
}

configure<JavaFXOptions> {
    version = "13"

    modules = listOf("javafx.controls", "javafx.fxml")
}

tasks.test {
    useJUnitPlatform()
    // I couldn't get a module to work for the test sourceSets
    jvmArgs("--add-exports", "org.junit.platform.commons/org.junit.platform.commons.util=ALL-UNNAMED",
            "--add-exports", "org.junit.platform.commons/org.junit.platform.commons.logging=ALL-UNNAMED")
}
