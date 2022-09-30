import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
}

version = "0.1.0"

dependencies {
    implementation(project(":main"))
    compileOnly("dev.arbjerg.lavalink:plugin-api:0.7.0")
    runtimeOnly("com.github.freyacodes.lavalink:Lavalink-Server:feature~plugins")
}

application {
    mainClass.set("org.springframework.boot.loader.JarLauncher")
}

tasks {
    shadowJar {
        archiveBaseName.set("allvaa-lpsources-plugin")
        archiveClassifier.set("")
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

