import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

version = "0.1.0"

dependencies {
    compileOnly("com.github.walkyst:lavaplayer-fork:1.3.98.4")
}

tasks {
    shadowJar {
        archiveBaseName.set("allvaa-lpsources")
        archiveClassifier.set("")
    }
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}
