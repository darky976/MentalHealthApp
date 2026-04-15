import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}

val localProperties = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) {
        f.inputStream().use { load(it) }
    }
}
/** Пусто в local.properties → подставляются твои проектные значения по умолчанию (можно переопределить). */
val supabaseUrlFromLocal: String = localProperties.getProperty("supabase.url", "").trim()
val supabaseUrlEffective: String =
    if (supabaseUrlFromLocal.isNotEmpty()) supabaseUrlFromLocal else "https://vlcypyocgvrshkvmljme.supabase.co"

val supabaseAnonFromLocal: String = localProperties.getProperty("supabase.anon.key", "").trim()
val supabaseAnonEffective: String =
    if (supabaseAnonFromLocal.isNotEmpty()) supabaseAnonFromLocal
    else "sb_publishable_5mAKD8Y2nvYysKoQZXn2QQ_34eT4yD9"

android {
    namespace = "com.example.mentalhealth"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.mentalhealth"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField(
            "String",
            "SUPABASE_URL",
            "\"${supabaseUrlEffective.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )
        buildConfigField(
            "String",
            "SUPABASE_ANON_KEY",
            "\"${supabaseAnonEffective.replace("\\", "\\\\").replace("\"", "\\\"")}\""
        )
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
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}



dependencies {




    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    implementation("com.squareup.okhttp3:okhttp:4.11.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    testImplementation("junit:junit:4.13.2")
}
