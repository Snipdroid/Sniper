package ren.imyan.sniper

import android.app.Application
import com.google.android.material.color.DynamicColors
import org.koin.android.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import org.koin.core.module.Module

class App : Application() {

    private val moduleList = listOf<Module>()

    override fun onCreate() {
        super.onCreate()
        initKoin()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    private fun initKoin() {
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@App)
//            modules(moduleList)
        }
    }
}