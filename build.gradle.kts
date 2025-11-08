buildscript {
    repositories {
        gradlePluginPortal()
        jcenter()
        google()
        mavenCentral()
        maven(url = "https://maven.fabric.io/public")

    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.20")
        classpath("com.android.tools.build:gradle:7.0.0-alpha01")
        classpath("com.google.gms:google-services:4.3.4")
        classpath("android.arch.navigation:navigation-safe-args-gradle-plugin:1.0.0")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.4.1")
    }
}
group = "cz.visualio.archeologie"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}
