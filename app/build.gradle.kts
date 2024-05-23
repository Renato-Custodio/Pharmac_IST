plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    alias(libs.plugins.googleGmsGoogleServices)
}

secrets {
    propertiesFileName = "secrets.properties"
    defaultPropertiesFileName = "local.defaults.properties"

    // Configure which keys should be ignored by the plugin by providing regular expressions.
    // "sdk.dir" is ignored by default
    ignoreList.add("sdk.*")       // Ignore all keys matching the regexp "sdk.*"
}

android {
    namespace = "pt.ulisboa.tecnico.cmov.pharmacist"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    defaultConfig {
        applicationId = "pt.ulisboa.tecnico.cmov.pharmacist"
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.material)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.coordinatorlayout)
    implementation(libs.retrofit)
    implementation(libs.logging.interceptor)
    implementation(libs.gson)
    implementation(libs.converter.gson)
    implementation(libs.places)
    implementation("com.basgeekball:awesome-validation:4.3")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation(libs.firebase.database)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.ui.storage.v720)
    implementation(libs.legacy.support.v4)
    annotationProcessor(libs.glide.compiler)
    implementation(libs.camera.view)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}