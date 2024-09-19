plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
    id("signing")

    jacoco
}

jacoco {
    toolVersion = "0.8.12"
    reportsDirectory = layout.buildDirectory.dir("reports/jacoco")
}

android {
    namespace = "com.example.vciclient"
    compileSdk = 34

    defaultConfig {
        minSdk = 23

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.nimbusds:nimbus-jose-jwt:9.37.3")
    implementation("io.fusionauth:fusionauth-jwt:5.3.2")
    implementation("com.google.code.gson:gson:2.10.1")

    //Parse CBOR Data
    implementation("co.nstant.in:cbor:0.9")

    //Build Json Object
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.0")

    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.2")
    testImplementation("org.json:json:20231013")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}

tasks {
    register<JacocoReport>("jacocoTestReport") {
        dependsOn(
            listOf(
                "testDebugUnitTest",
                "compileReleaseUnitTestKotlin",
                "testReleaseUnitTest"
            )
        )

        reports {
            html.required = true
        }
        sourceDirectories.setFrom(layout.projectDirectory.dir("src/main/java"))
        classDirectories.setFrom(
            files(
                fileTree(layout.buildDirectory.dir("intermediates/javac/debug")),
                fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug"))
            )
        )
        executionData.setFrom(files(
            fileTree(layout.buildDirectory) { include(listOf("**/testDebug**.exec")) }
        ))

    }
}

tasks.build {
    finalizedBy("jacocoTestReport")
}

apply {
    from("publish-artifact.gradle")
}