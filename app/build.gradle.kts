plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.androidx.navigation.safe.args)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.google.firebase.crashlytics)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.ksp)
    id("kotlin-parcelize")
}

android {
    namespace = "com.humans.body.generator.retake.reshothumans.photoeditor.faceswapping"
    compileSdk = 36

//    flavorDimensions += "env"
//    productFlavors {
//        create("prod") {
//            dimension = "env"
//            applicationId = "com.hni.faceapp.bodyeditor.bodytune.photoeditor.humanart.bodypose"
//            versionNameSuffix = ""
//        }
//        create("dev") {
//            dimension = "env"
//            applicationId = "com.test.human"
//            versionNameSuffix = "-debug"
//        }
//    }

    defaultConfig {
        applicationId = "com.hni.faceapp.bodyeditor.bodytune.photoeditor.humanart.bodypose"
        minSdk = 24
        targetSdk = 35
        versionCode = 14
        versionName = "1.1.4"
        setProperty("archivesBaseName", "Human Body Generator $versionName ($versionCode)")
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        multiDexEnabled = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "admobSDKKey", "ca-app-pub-2925173722220817~3882439959")
            resValue("string", "admob_banner_id", "ca-app-pub-2925173722220817/6675668372")
            resValue("string", "admob_native_id", "ca-app-pub-2925173722220817/2993296431")
            resValue("string", "admob_interstitial_id", "ca-app-pub-2925173722220817/3728735558")
            resValue("string", "admobRewardAd", "ca-app-pub-2925173722220817/7971232899")
            resValue("string", "admobRewardAdsInitialID", "ca-app-pub-2925173722220817/7971232899")
            resValue("string", "admob_openAd_id", "ca-app-pub-2925173722220817/3136880636")
            resValue("string", "admobOnboardingNativeAd", "ca-app-pub-2925173722220817/2993296431")
        }


        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            resValue("string", "admobSDKKey", "ca-app-pub-3940256099942544~3347511713")
            resValue("string", "admob_banner_id", "ca-app-pub-3940256099942544/9214589741")
            resValue("string", "admob_native_id", "ca-app-pub-3940256099942544/2247696110")
            resValue("string", "admob_interstitial_id", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "admobRewardAd", "ca-app-pub-3940256099942544/5224354917")
            resValue("string", "admobRewardAdsInitialID", "ca-app-pub-3940256099942544/5354046379")
            resValue("string", "admob_openAd_id", "ca-app-pub-3940256099942544/9257395921")
            resValue("string", "admobOnboardingNativeAd", "ca-app-pub-3940256099942544/2247696110")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlinOptions {
        jvmTarget = "21"
    }
    bundle {
        language {
            enableSplit = false
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform("org.jetbrains.kotlin:kotlin-bom:2.2.20"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.firebase.config)
    implementation(libs.firebase.database)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    implementation(libs.sdp.android)
    implementation(libs.ssp.android)
    implementation(libs.flexbox)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.glide)
    implementation(libs.logging.interceptor)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.billing.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.facebook.shimmer)
    implementation("com.airbnb.android:lottie:6.6.6")

    implementation(libs.timber)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.play.services.ads)
    implementation("com.zeugmasolutions.localehelper:locale-helper-android:1.5.1") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-android-extensions-runtime")
    }

    implementation("com.google.ai.client.generativeai:generativeai:0.4.0")
    implementation("com.github.yalantis:ucrop:2.2.6")
    implementation("com.github.skydoves:colorpickerview:2.2.4")
    implementation(project(":sticker"))
//    implementation(project(":Roozi"))
}
