import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.32"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "me.allvaa"
version = "0.1.0"

repositories {
    maven("https://jitpack.io")
    mavenCentral()
}

dependencies {
    compileOnly("com.github.walkyst:lavaplayer-fork:1.3.98.4")
    compileOnly(kotlin("stdlib"))
}

tasks {
    shadowJar {
        archiveBaseName.set("allvaa-lpsc")
        archiveClassifier.set("")
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
