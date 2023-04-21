plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("maven-publish")
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
        val ktorVersion = "2.2.4"
        val korioVersion = "3.4.0"
        val coroutinesVersion = "1.6.4"
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:multik-core:0.2.1")
                implementation("org.jetbrains.kotlinx:multik-kotlin:0.2.1")
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
                implementation("androidx.work:work-runtime-ktx:2.8.1")
                implementation("androidx.concurrent:concurrent-futures-ktx:1.1.0")
            }
        }
        val androidUnitTest by getting {
            val junitVersion = "4.13.2"
            dependencies {
                implementation("junit:junit:$junitVersion")
                implementation("androidx.test:core:1.5.0")
                implementation("androidx.test.ext:junit:1.1.5")
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
    compileSdk = 33
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
dependencies {
    androidTestImplementation(project(mapOf("path" to ":shared")))
}
