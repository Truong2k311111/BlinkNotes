plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.blinknotes"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.blinknotes"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
         isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/INDEX.LIST",
                "mozilla/public-suffix-list.txt"
            )
        }
    }
    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "io.grpc") {
                useVersion("1.62.2")
            }
        }
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.androidx.navigation.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.storage)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.animation.core.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.play.services.fido)
    implementation(libs.firebase.crashlytics.buildtools)
    implementation(libs.play.services.fido)
    implementation(libs.play.services.fido)
    implementation(libs.play.services.fido)
    implementation(libs.androidx.foundation.layout.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation ("io.coil-kt:coil-compose:2.7.0")

    implementation ("com.google.accompanist:accompanist-pager:0.30.1")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.30.1")

    implementation ("com.google.firebase:firebase-auth:22.1.1")
    implementation ("com.google.firebase:firebase-firestore")

    implementation ("com.google.firebase:firebase-storage")
    implementation ("com.google.android.gms:play-services-auth:20.7.0")

    implementation ("com.google.mlkit:barcode-scanning:17.0.0")

    implementation ("com.google.accompanist:accompanist-permissions:0.19.0")


    implementation ("com.google.guava:guava:31.0.1-android")
    implementation ("com.google.android.gms:play-services-safetynet:18.0.1")



    implementation("com.vanniktech:emoji-google:0.15.0")
    implementation("com.vanniktech:emoji-ios:0.15.0")

    implementation ("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.github.bumptech.glide:glide:4.15.1")
    implementation("com.github.bumptech.glide:okhttp3-integration:4.15.1")
    implementation("com.github.bumptech.glide:annotations:4.15.1")
    implementation ("com.google.firebase:firebase-bom:32.7.4")

    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.google.firebase:firebase-database:20.3.0")
    implementation ("com.google.firebase:firebase-analytics:21.6.1")

    implementation ("com.google.firebase:firebase-messaging:23.4.1")

    implementation ("com.google.firebase:firebase-common")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.35.0")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")
    implementation("io.grpc:grpc-core:1.62.2")
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")

    // Dagger Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")

    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation ("com.squareup.okhttp3:okhttp:4.12.0")

    // Coil & Glide
    implementation ("io.coil-kt:coil-compose:2.7.0")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("com.github.bumptech.glide:okhttp3-integration:4.15.1")
    implementation ("com.github.bumptech.glide:annotations:4.15.1")

    // Google Services
    implementation ("com.google.android.gms:play-services-auth:20.7.0")
    implementation ("com.google.android.gms:play-services-safetynet:18.0.1")
    implementation ("com.google.android.gms:play-services-fido")

    // ML Kit
    implementation ("com.google.mlkit:barcode-scanning:17.0.0")

    // Accompanist
    implementation ("com.google.accompanist:accompanist-pager:0.30.1")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.30.1")
    implementation ("com.google.accompanist:accompanist-permissions:0.19.0")

    // Emoji
    implementation ("com.vanniktech:emoji-google:0.15.0")
    implementation ("com.vanniktech:emoji-ios:0.15.0")

    // Other
    implementation ("com.google.guava:guava:31.0.1-android")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("com.google.auth:google-auth-library-oauth2-http:1.35.0")
    implementation ("io.grpc:grpc-core:1.62.2")
    coreLibraryDesugaring ("com.android.tools:desugar_jdk_libs:2.0.4")

    // Testing
    testImplementation ("junit:junit:4.13.2")
    androidTestImplementation ("androidx.test.ext:junit:1.1.5")
    androidTestImplementation ("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation ("androidx.compose.ui:ui-test-junit4")
    debugImplementation ("androidx.compose.ui:ui-tooling")
    debugImplementation ("androidx.compose.ui:ui-test-manifest")

    implementation ("com.google.android.gms:play-services-location:21.0.1")

}

