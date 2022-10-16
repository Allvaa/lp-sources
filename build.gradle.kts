plugins {
    kotlin("jvm") version "1.7.20"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

allprojects {
    group = "me.allvaa"

    repositories {
        maven("https://jitpack.io")
        maven("https://m2.dv8tion.net/releases")
        mavenCentral()
        jcenter()
    }
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "kotlin")
    apply(plugin = "com.github.johnrengelman.shadow")

    dependencies {
        compileOnly(kotlin("stdlib"))
    }
}
