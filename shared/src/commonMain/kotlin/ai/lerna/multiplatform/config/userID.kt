package ai.lerna.multiplatform.config

expect class UserID() {
    fun getUniqueId(context: KMMContext): Int
}