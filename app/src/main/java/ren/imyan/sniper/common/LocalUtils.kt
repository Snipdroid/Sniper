package ren.imyan.sniper.common

import android.content.Context
import android.os.Build
import java.util.Locale

object LocalUtils {
    fun getCurrentLocale(context: Context): Locale {
        return context.resources.configuration.locales[0]
    }
}