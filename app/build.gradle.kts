plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.huflit.studentmanagement"
    compileSdk = 34

    packagingOptions{
        exclude("META-INF/NOTICE.md")
        exclude ("META-INF/LICENSE.md")
    }

    defaultConfig {
        applicationId = "com.huflit.studentmanagement"
        minSdk = 28
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-firestore:25.0.0")
    implementation(files("/Users/douglas/Coding Projects/AndroidProjects/StudentManagement/zpdk-release-v3.1.aar"))
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    //Mail
    implementation("com.sun.mail:android-mail:1.6.6")
    implementation("com.sun.mail:android-activation:1.6.7")

    //PhotoView
    implementation("com.github.chrisbanes:PhotoView:2.3.0")

    //GSon
    implementation("com.google.code.gson:gson:2.8.6")

    //Rounded ImageView
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("de.hdodenhof:circleimageview:3.1.0")

    //Excel
    implementation ("org.apache.poi:poi:5.2.3")
    implementation ("org.apache.poi:poi-ooxml:5.2.3")
    implementation ("org.apache.commons:commons-collections4:4.4")
    implementation ("org.apache.xmlbeans:xmlbeans:5.1.1")

    //zalo pay
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("commons-codec:commons-codec:1.14")

    //chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
}