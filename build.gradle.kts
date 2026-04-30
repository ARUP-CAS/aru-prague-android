buildscript {
    repositories {
        gradlePluginPortal()
        google()
        jcenter()
        mavenLocal()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://www.jitpack.io")
        maven(url = "https://maven.fabric.io/public")

    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
        maven(url = "https://jitpack.io")
        mavenLocal()
        maven(url = "https://www.jitpack.io")

    }
}

group = "cz.visualio.sauersack"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    google()
    jcenter()
    maven(url = "https://jitpack.io")
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
