package ren.imyan.sniper

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import ren.imyan.sniper.net.netModule
import timber.log.Timber

class App : Application() {

    private val moduleList = listOf(store, netModule)

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        initKoin()
    }

    private fun initKoin() {
        startKoin {
            modules(moduleList)
            androidContext(this@App)
        }
    }
}