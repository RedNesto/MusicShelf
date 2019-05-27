import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openjfx.gradle.JavaFXOptions

plugins {
    kotlin("jvm") version "1.3.30"

    id("org.openjfx.javafxplugin") version "0.0.7"
    id("org.beryx.jlink") version "2.10.2"
}

group = "io.github.rednesto"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains:annotations:17.0.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "12"
}

application {
    mainClassName = "musicshelf/io.github.rednesto.musicshelf.MusicShelfApp"
}

configure<JavaFXOptions> {
    version = "12"

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
