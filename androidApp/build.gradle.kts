plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.firebaseCrashlytics)
    alias(libs.plugins.googleServices)
    alias(libs.plugins.navigationSafeArgs)
    id("org.jetbrains.kotlin.plugin.parcelize") version "2.1.21"
}



android {

    namespace = "cz.visualio.sauersack.androidApp"
    buildFeatures {
        viewBinding = true
    }
    compileSdk = 36
    defaultConfig {
        applicationId = "cz.visualio.sauersack.androidApp"
        minSdk = 24
        targetSdk = 36
        versionCode = 40
        versionName = "4.0"

        //  to fix play market issue
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a", "x86_64")
        }
    }


//    signingConfigs {
//        create("release") {
//            val properties = Properties().apply {
//                load(File("keystore.properties").reader())
//            }
//            storeFile = rootProject.file("sauersack.jks")
//            storePassword = properties.getProperty("storePassword")
//            keyPassword = properties.getProperty("keyPassword")
//            keyAlias = properties.getProperty("keyAlias")
//        }
//    }

    buildTypes {
        getByName("release") {
            //  signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
        }

        getByName("debug") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

}

dependencies {
    val arrowVersion: String by project

    implementation(platform(libs.firebase.bom))

    implementation(libs.material)
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation(libs.peko)

    implementation("io.arrow-kt:arrow-fx:$arrowVersion")
    implementation("io.arrow-kt:arrow-optics:$arrowVersion")
    implementation("io.arrow-kt:arrow-generic:$arrowVersion")
    implementation("io.arrow-kt:arrow-syntax:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")

    implementation(libs.retrofit.serialization)
    implementation(libs.retrofit)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    implementation(libs.navigation.runtime.ktx)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
//    implementation("androidx.navigation:navigation-dynamic-features-fragment:$navigationVersion")

    implementation(libs.kotlin.stdlib.v190)
    implementation(libs.play.services.maps.v1810)
    implementation(libs.legacy.support.v4)


    implementation(libs.google.firebase.analytics)

    implementation(libs.serialization.core)
    implementation(libs.serialization.json)
    implementation(libs.maps.ktx)
    implementation(libs.touchimageview)

    implementation(libs.maps.utils.ktx.v220)
    implementation(libs.glide.v4110)
    implementation(libs.firebase.crashlytics)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    ksp(libs.glideksp)

    implementation(libs.dotsindicator)
}

/*tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
        freeCompilerArgs += "-Xextended-compiler-checks"
    }
}*/

kotlin {
    sourceSets.main {
        kotlin.srcDir("build/generated/ksp/main/kotlin")
    }
}

apply {
    apply(from = rootProject.file("gradle/generated-kotlin-sources.gradle"))
}

