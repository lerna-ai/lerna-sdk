package ai.lerna.multiplatform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform