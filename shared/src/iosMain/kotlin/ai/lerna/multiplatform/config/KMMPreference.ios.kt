package ai.lerna.multiplatform.config

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings


actual fun KMMPreference(context: KMMContext): Settings {
    return NSUserDefaultsSettings.Factory().create("Lerna_settings")
}