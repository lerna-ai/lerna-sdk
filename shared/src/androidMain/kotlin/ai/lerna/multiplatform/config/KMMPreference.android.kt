package ai.lerna.multiplatform.config

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings


actual fun KMMPreference(context: KMMContext): Settings {
    return SharedPreferencesSettings.Factory(context).create("Lerna_settings")
}