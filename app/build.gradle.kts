plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.motivationalquote"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.motivationalquote"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        // ✅ Only XML/ViewBinding
        viewBinding = true
        // ❌ Remove Compose
        // compose = true
    }
}

    dependencies {
        implementation("androidx.recyclerview:recyclerview:1.3.0")
        implementation("androidx.cardview:cardview:1.0.0")
        implementation("androidx.multidex:multidex:2.0.1")

        implementation("androidx.cardview:cardview:1.0.0")

        // ✅ Only one AdMob dependency
        implementation("com.google.android.gms:play-services-ads:23.0.0")

        // ✅ Firebase BOM (keeps versions consistent)
        implementation(platform("com.google.firebase:firebase-bom:32.7.2"))

        // Firebase services
        implementation("com.google.firebase:firebase-analytics")
        implementation("com.google.firebase:firebase-auth")
        implementation("com.google.firebase:firebase-database")
        implementation("com.google.firebase:firebase-firestore-ktx")

        // Kotlin Coroutines for Firebase/Play Services
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

        // AndroidX / UI (XML-based)
        implementation("androidx.core:core-ktx:1.13.1")
        implementation("androidx.appcompat:appcompat:1.7.0")
        implementation("com.google.android.material:material:1.12.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")

        // Testing
        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.2.1")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    }
