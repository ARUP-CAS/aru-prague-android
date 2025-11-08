plugins {
    id("com.android.application")
    kotlin("android")
//    id("kotlin-android")
    id("kotlin-parcelize")
    kotlin("kapt")
}

group = "cz.visualio.archeologie"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    google()
    jcenter()
    mavenCentral()
    maven(url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/")// for SNAPSHOT builds
    maven(url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven(url = "https://kotlin.bintray.com/kotlinx/") // soon will be just jcenter()

}
dependencies {
    val navigationVersion: String by project
    val leakCanaryVersion: String by project
    val arrowVersion: String by project
    val kotlinVersion: String by project

    implementation(project(":shared"))
    implementation(platform("com.google.firebase:firebase-bom:25.12.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    implementation("com.google.android.material:material:1.2.1")
    implementation("androidx.appcompat:appcompat:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout:2.0.4")
    implementation("com.markodevcic.peko:peko:2.1.2")


    implementation("io.arrow-kt:arrow-fx:$arrowVersion")
    implementation("io.arrow-kt:arrow-optics:$arrowVersion")
    implementation("io.arrow-kt:arrow-generic:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")

    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")

    implementation("androidx.navigation:navigation-runtime-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
    implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")
//    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navigationVersion")

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("com.google.android.gms:play-services-maps:17.0.0")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
//    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    debugImplementation("com.squareup.leakcanary:leakcanary-android:$leakCanaryVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")
    implementation("com.google.maps.android:maps-ktx:2.1.3")

    implementation("com.google.maps.android:maps-utils-ktx:2.2.0")
    implementation("com.github.bumptech.glide:glide:4.11.0")
    kapt("com.github.bumptech.glide:compiler:4.11.0")

    implementation("com.tbuonomo.andrui:viewpagerdotsindicator:4.1.2")
}
android {
    buildFeatures {
        viewBinding = true
    }
    compileSdkVersion(30)
    defaultConfig {
        applicationId = "cz.visualio.archeologie.androidApp"
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 14
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        useIR = true
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}

apply {
//    plugin("kotlin-android")
//    plugin("kotlin-android-extensions")
    plugin("androidx.navigation.safeargs.kotlin")
    plugin("com.google.gms.google-services")
    plugin("com.google.firebase.crashlytics")
    apply(from = rootProject.file("gradle/generated-kotlin-sources.gradle"))
}