plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "br.com.ifsp.gisotra.microredesocial"
    compileSdk = 35

    defaultConfig {
        applicationId = "br.com.ifsp.gisotra.microredesocial"
        minSdk = 33
        targetSdk = 35
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures{
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // --- INÍCIO DA CONFIGURAÇÃO DO FIREBASE ---
    // 1. O BoM entra aqui gerenciando as versões (usa a 32.8.0 ou superior)
    implementation(platform("com.google.firebase:firebase-bom:32.8.0"))

    // 2. Trocamos os 'libs.firebase' pela declaração direta em texto, SEM a versão no final
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    // --- FIM DA CONFIGURAÇÃO DO FIREBASE ---

    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.play.services.location)
    implementation(libs.play.services.geocoder)
    implementation(libs.cronet.embedded)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}