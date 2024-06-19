plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.android.library")
    id("maven-publish")
}

group = "ai.lerna.multiplatform"
version = "0.0.7"

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
                implementation("org.jetbrains.kotlinx:multik-core:0.2.3")
                implementation("org.jetbrains.kotlinx:multik-kotlin:0.2.3")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")
                implementation("io.ktor:ktor-client-core:$ktorVersion")
                implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")
                implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
                implementation("io.ktor:ktor-client-cio:$ktorVersion")
                implementation("io.ktor:ktor-network:$ktorVersion")
                implementation("io.ktor:ktor-network-tls:$ktorVersion")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common:1.9.22")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("io.github.aakira:napier:2.6.1")
                implementation("com.soywiz.korlibs.korio:korio:$korioVersion")
                implementation("com.soywiz.korlibs.krypto:krypto:$korioVersion")
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
                implementation("org.robolectric:robolectric:4.10")
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
    androidTestImplementation(project(mapOf("path" to ":lerna-kmm-sdk")))
}
