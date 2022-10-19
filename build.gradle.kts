plugins {
    //trick: for the same plugin versions in all sub-modules
    id("com.android.library").version("7.2.2").apply(false)
    kotlin("plugin.serialization").version("1.7.20").apply(false)
    kotlin("multiplatform").version("1.7.20").apply(false)
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
