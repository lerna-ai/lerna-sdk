@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    id("maven-publish")
}

/*tasks{
    withType<Kotlin2JsCompile> {
        kotlinOptions.freeCompilerArgs += listOf("-Xskip-prerelease-check", "-Xexpect-actual-classes")
    }
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "21"
        kotlinOptions.freeCompilerArgs += listOf("-Xexpect-actual-classes")
    }
}*/

group = "ai.lerna.multiplatform"
version = "0.0.7"

kotlin {

    tasks.register("testClasses")

    androidTarget {
        publishLibraryVariants("release", "debug")
        withSourcesJar(publish = false)
    }


    wasmJs {
        outputModuleName = "composeLerna"
        browser {
            commonWebpackConfig {
                outputFileName = "composeLerna.js"

            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }

    val iosTarget: (String, KotlinNativeTarget.() -> Unit) -> KotlinNativeTarget = when {
        System.getenv("SDK_NAME")?.startsWith("iphoneos") == true -> ::iosArm64
        System.getenv("NATIVE_ARCH")?.startsWith("arm") == true -> ::iosSimulatorArm64
        else -> ::iosX64
    }
    iosTarget("ios") {
        binaries {
            framework {
                baseName = "shared"
//		export(project(":advancedml"))
//		embedBitcode("disable")
            }
        }
    }

//    listOf(
//        iosX64(),
//        iosArm64(),
//        iosSimulatorArm64()
//	tvosArm64(),
//        tvosX64(),
//        tvosSimulatorArm64()
//    ).forEach {
//        it.binaries.framework {
//            baseName = "shared"
//            embedBitcode("disable")
//        }
//    }


    applyDefaultHierarchyTemplate()

    sourceSets {
        commonMain.dependencies {
            implementation(project(":advancedml"))
            implementation(libs.korge)
            implementation(libs.multiplatform.settings)
            implementation(libs.multik.core)
            implementation(libs.multik.kotlin)
            implementation(libs.kotlinx.datetime)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.kotlin.stdlib.common)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines.core)
            implementation(libs.napier)
            runtimeOnly(libs.ktor.utils)
        }
        commonTest.dependencies {
            implementation(libs.kotlinx.coroutines.test)
            implementation(kotlin("test"))
        }

        androidMain.dependencies {
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.androidx.runtime)
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.network)
            implementation(libs.ktor.network.tls)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.androidx.work.runtime.ktx)
            implementation(libs.androidx.concurrent.futures.ktx)
        }
        val androidUnitTest by getting {
            dependencies {
                implementation(libs.junit)
                implementation(libs.androidx.core)
                implementation(libs.androidx.junit)
                implementation(libs.robolectric)
                implementation(libs.testng)
                implementation(libs.kotlinx.coroutines.android)
            }
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.cio)
            implementation(libs.ktor.network)
            implementation(libs.ktor.network.tls)
            implementation(libs.ktor.client.darwin)
        }
        wasmJsMain.dependencies {
            implementation(libs.kotlinx.browser)
        }
        wasmJsTest.dependencies {
            implementation(libs.kotlinx.browser)
        }
    }
    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
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

android {
    testOptions.unitTests.isIncludeAndroidResources = true
    namespace = "ai.lerna.multiplatform"
    compileSdk = 35
    defaultConfig {
        minSdk = 23
    }
    compileOptions {
        targetCompatibility = JavaVersion.VERSION_21
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

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

val packForXcode by tasks.registering(Sync::class, fun Sync.() {
    group = "build"
    val mode = System.getenv("CONFIGURATION") ?: "DEBUG"
    val framework = kotlin.targets.getByName<KotlinNativeTarget>("ios").binaries.getFramework(mode)
    inputs.property("mode", mode)
    dependsOn(framework.linkTaskProvider)
    val targetDir = File(project.layout.buildDirectory.get().asFile, "xcode-frameworks")
    from({ framework.outputDirectory })
    into(targetDir)
})
tasks.getByName("build").dependsOn(packForXcode)
