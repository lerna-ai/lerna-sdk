import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("maven-publish")
}
tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}
group = "ai.lerna.multiplatform"
version = "0.0.8"

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        withSourcesJar(publish = false)
    }

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
        val ktorVersion = "2.3.0"
        val korioVersion = "3.4.0"
        val coroutinesVersion = "1.6.4"
        val commonMain by getting {
            dependencies {
                implementation(libs.multik.core)
                implementation(libs.multik.kotlin)
                implementation(libs.kotlinx.datetime)
                implementation(libs.ktor.client.core)
                implementation(libs.ktor.client.content.negotiation)
                implementation(libs.ktor.serialization.kotlinx.json)
                implementation(libs.ktor.client.cio)
                implementation(libs.ktor.network)
                implementation(libs.ktor.network.tls)
                implementation(libs.kotlin.stdlib.common)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.napier)
                implementation(libs.korio)
                implementation(project(":advancedml"))
                runtimeOnly(libs.ktor.utils)
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val androidMain by getting {
            dependencies {
                implementation(libs.ktor.client.okhttp)
                implementation(libs.androidx.work.runtime.ktx)
                implementation(libs.androidx.concurrent.futures.ktx)
            }
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
                implementation(libs.robolectric.v410)
                implementation(libs.testng)
                implementation(libs.kotlinx.coroutines.android)
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
                implementation(libs.ktor.client.darwin)
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

    publishing {
        repositories {
            maven {
                if (project.hasProperty("repoURL")) {
                        println(project.properties["repoURL"])
                }
                // Release Repo
                //url = uri("https://lerna-ai-470158444867.d.codeartifact.us-east-1.amazonaws.com/maven/release/")
                // Development Repo
                url = uri("https://lerna-dev-470158444867.d.codeartifact.us-east-1.amazonaws.com/maven/lerna-dev/")
                credentials {
                    username = "aws"
                    // to generate password run `aws codeartifact get-authorization-token --region=us-east-1 --domain lerna-dev --query authorizationToken --output text`
                    password = "update-password-here"
                }
            }
        }
        publications {
            withType<MavenPublication> {
                pom {
                    packaging = "aar"
                    name.set("Lerna MultiPlatform SDK")
                    description.set("A multiplatform native implementation of Lerna SDK")
                    url.set("https://lerna.ai")
                    licenses {
                        license {
                            name.set("The Lerna license")
                            url.set("https://lerna.ai/library/license")
                        }
                    }
                    organization {
                        name.set("Lerna Inc")
                        url.set("https://lerna.ai/")
                    }
                }
            }
        }
    }
}

android {
    testOptions.unitTests.isIncludeAndroidResources = true
    namespace = "ai.lerna.multiplatform"
    compileSdk = 33
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            isMinifyEnabled = false
        }
    }
}
dependencies {
    implementation(project(":advancedml"))
}
