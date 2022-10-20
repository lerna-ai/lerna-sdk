package ai.lerna.multiplatform.config

expect class userID {
    fun getUniqueId(context: KMMContext): Int
}