import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("kotlin-android-extensions")
    kotlin("kapt")
    kotlin("plugin.serialization") version "1.4.20"
}


group = "cz.visualio.archeologie"
version = "1.0-SNAPSHOT"

repositories {
    gradlePluginPortal()
    google()
    jcenter()
    mavenCentral()
    maven(url = "https://kotlin.bintray.com/kotlinx/") // soon will be just jcenter()


    maven (url = "https://dl.bintray.com/arrow-kt/arrow-kt/")
    maven (url = "https://oss.jfrog.org/artifactory/oss-snapshot-local/")// for SNAPSHOT builds
}

kotlin {
    android{
    }

    dependencies {
        val arrowVersion: String by project
        "kapt"("io.arrow-kt:arrow-meta:$arrowVersion")
    }

    ios {
        binaries {
            framework {
                baseName = "shared"
            }
        }
    }
    sourceSets {
        val commonMain by getting {

            dependencies {
                val coroutinesVersion: String by project
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.1.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val androidMain by getting {

            apply {
                apply(from = rootProject.file("gradle/generated-kotlin-sources.gradle"))
            }

            dependencies {
                val arrowVersion: String by project
                val navigationVersion: String by project

                implementation("com.google.android.material:material:1.2.1")

                implementation("io.arrow-kt:arrow-fx:$arrowVersion")
                implementation("io.arrow-kt:arrow-optics:$arrowVersion")
                implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
                implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")

                implementation("androidx.navigation:navigation-fragment-ktx:$navigationVersion")
                implementation("androidx.navigation:navigation-ui-ktx:$navigationVersion")

                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.0.1")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.1")

                implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:0.8.0")
                implementation("com.squareup.retrofit2:retrofit:2.9.0")
            }
        }
        val androidTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
                implementation("junit:junit:4.13.1")
            }
        }
        val iosMain by getting
        val iosTest by getting
    }
}
android {

    compileSdkVersion(30)
    sourceSets["main"].manifest.srcFile("src\\androidMain\\AndroidManifest.xml")
    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(30)
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }

}
val packForXcode by tasks.creating(Sync::class) {
    group = "build"
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val sdkName = System.getenv("SDK_NAME") ?: "iphonesimulator"
    val targetName = "ios" + if (sdkName.startsWith("iphoneos")) "Arm64" else "X64"
    val framework =
        kotlin.targets.getByName<KotlinNativeTarget>(targetName).binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTask)
    val targetDir = File(buildDir, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)
}
tasks.getByName("build").dependsOn(packForXcode)
dependencies {
    implementation("com.google.android.gms:play-services-maps:17.0.0")
}


tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        useIR = false
        jvmTarget = JavaVersion.VERSION_1_8.toString()
    }
}
