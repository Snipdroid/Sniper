package ren.imyan.sniper

import android.app.Application
import com.google.android.material.color.DynamicColors
import org.koin.android.BuildConfig
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import ren.imyan.sniper.common.DataStoreUtil
import ren.imyan.sniper.net.netModule
import timber.log.Timber

class App : Application() {

    private val moduleList = listOf(netModule)

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        DataStoreUtil.init(this)
        initKoin()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }

    private fun initKoin() {
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@App)
            modules(moduleList)
        }
    }
}