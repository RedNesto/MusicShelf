import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openjfx.gradle.JavaFXOptions

plugins {
    kotlin("jvm") version "1.3.50"

    id("org.openjfx.javafxplugin") version "0.0.8"
    id("org.beryx.jlink") version "2.16.1"
    id("com.novoda.build-properties") version "0.4.1"
}

group = "io.github.rednesto"
version = "1.0-SNAPSHOT"

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
    implementation("org.jetbrains:annotations:17.0.0")
    implementation("org.spongepowered:configurate-xml:3.6")
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

jlink {
    addOptions("--strip-debug", "--no-header-files", "--no-man-pages")
    imageName.set("MusicShelf")
    launcher {
        name = "MusicShelf"
    }
    jpackage {
        jvmArgs.clear()
        installerOptions = listOf("--win-per-user-install", "--win-menu", "--win-shortcut")
    }
}
