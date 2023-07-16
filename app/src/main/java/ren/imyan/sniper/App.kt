package ren.imyan.sniper

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.android.material.color.utilities.DynamicColor

class App : Application() {

    override fun onCreate() {
        super.onCreate()

        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}