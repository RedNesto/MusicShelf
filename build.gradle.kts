import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.openjfx.gradle.JavaFXOptions

plugins {
    kotlin("jvm") version "1.3.70"

    id("application")
    id("org.openjfx.javafxplugin") version "0.0.8"
    id("com.novoda.build-properties") version "0.4.1"
}

buildProperties.create("local") { using(file("local-gradle.properties")) }

dependencies {
    api(kotlin("stdlib-jdk8"))
    api("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.4")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-javafx:1.3.4") {
        exclude("org.openjfx") // They transitively import JFX 11 with linux binaries
    }
    api("org.jetbrains:annotations:19.0.0")
    api("org.spongepowered:configurate-xml:3.6.1")
    api("org.spongepowered:configurate-hocon:3.6.1")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.6.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.6.0")
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.javamodularity.moduleplugin")

    repositories {
        jcenter()
        maven {
            name = "sponge"
            url = uri("https://repo.spongepowered.org/maven")
        }
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "13"
    }
}

application {
    mainClass.set("musicshelf/io.github.rednesto.musicshelf.MusicShelfApp")
}

distributions.main {
    contents.into("plugins") {
        pluginProjects().forEach { pluginProject -> pluginProject.afterEvaluate { from(tasks.getByName("jar")) } }
    }
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

val run by tasks.existing(JavaExec::class) {
    workingDir("run")
}

pluginProjects().forEach { pluginProject ->
    pluginProject.afterEvaluate {
        val jarTask = tasks.getByName("jar")
        run.configure {
            classpath(jarTask)
            dependsOn(jarTask)
        }
    }
}

fun pluginProjects(): Collection<Project> = project("plugins").childProjects.values
