import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("maven-publish")
    id("com.codingfeline.buildkonfig") version "0.13.3"
}

kotlin {
    android()

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "shared"
            embedBitcode("disable")
        }
    }

    sourceSets {
        val ktorVersion = "2.1.3"
        val korioVersion = "3.3.1"
        val coroutinesVersion = "1.6.4"
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:multik-core:0.2.0")
                implementation("org.jetbrains.kotlinx:multik-kotlin:0.2.0")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-network:$ktorVersion")
                implementation("io.ktor:ktor-network-tls:$ktorVersion")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("io.github.aakira:napier:2.6.1")
                implementation("com.soywiz.korlibs.korio:korio:$korioVersion")
                runtimeOnly("io.ktor:ktor-utils:$ktorVersion")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:$ktorVersion")
                implementation("androidx.work:work-runtime-ktx:2.7.1")
                implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")
            }
        }
        val androidTest by getting {
            val junitVersion = "4.13.2"
            dependencies {
                implementation("junit:junit:$junitVersion")
                implementation("androidx.test:core:1.5.0")
                implementation("androidx.test.ext:junit:1.1.4")
                implementation("org.robolectric:robolectric:4.2.1")
                implementation("org.testng:testng:7.4.0")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:$coroutinesVersion")
            }
        }
        val iosX64Main by getting
        val iosArm64Main by getting
        val iosSimulatorArm64Main by getting
        val iosMain by creating {
            dependsOn(commonMain)
            iosX64Main.dependsOn(this)
            iosArm64Main.dependsOn(this)
            iosSimulatorArm64Main.dependsOn(this)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:$ktorVersion")
            }
        }
        val iosX64Test by getting
        val iosArm64Test by getting
        val iosSimulatorArm64Test by getting
        val iosTest by creating {
            dependsOn(commonTest)
            iosX64Test.dependsOn(this)
            iosArm64Test.dependsOn(this)
            iosSimulatorArm64Test.dependsOn(this)
        }
    }
}

android {
    testOptions.unitTests.isIncludeAndroidResources = true
    namespace = "ai.lerna.multiplatform"
    compileSdk = 32
    defaultConfig {
        minSdk = 26
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }
}

buildkonfig {
    packageName = "ai.lerna.multiplatform"
    objectName = "LernaConfig"
    // exposeObjectWithName = "YourAwesomePublicConfig"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "MPC_SERVER", "https://api.dev.lerna.ai:3443/")
        buildConfigField(FieldSpec.Type.STRING, "FL_SERVER", "https://api.dev.lerna.ai:7357/api/v2/")
        buildConfigField(FieldSpec.Type.STRING, "UPLOAD_PREFIX", "public/kmm/debug/")
        buildConfigField(FieldSpec.Type.BOOLEAN, "LOG_SENSOR_DATA", "true")
    }
    defaultConfigs("release") {
        buildConfigField(FieldSpec.Type.STRING, "MPC_SERVER", "https://api.dev.lerna.ai:8081/")
        buildConfigField(FieldSpec.Type.STRING, "FL_SERVER", "https://api.dev.lerna.ai:8080/api/v2/")
        buildConfigField(FieldSpec.Type.STRING, "UPLOAD_PREFIX", "public/kmm/")
        buildConfigField(FieldSpec.Type.BOOLEAN, "LOG_SENSOR_DATA", "false")
    }
}
