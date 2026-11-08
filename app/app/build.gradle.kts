plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.act2_ud1"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "es.iespoligonosur.tupesa"
        minSdk = 24
        targetSdk = 37
        versionCode = 2           // NÚMERO INTERNO: SIEMPRE SUBE (1 → 2 → 3...)
        versionName = "2.0"       // VERSIÓN QUE VE EL USUARIO: H2 CON PERSISTENCIA, HTTPS Y UBICACIÓN

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}