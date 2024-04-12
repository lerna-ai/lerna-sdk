package ai.lerna.multiplatform.config

import com.russhwolf.settings.Settings
import com.russhwolf.settings.StorageSettings

actual fun KMMPreference(context: KMMContext): Settings {
    return StorageSettings()
}