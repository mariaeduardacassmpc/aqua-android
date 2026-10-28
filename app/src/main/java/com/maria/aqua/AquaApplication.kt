package com.maria.aqua

import android.app.Application
import com.maria.aqua.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class AquaApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@AquaApplication)
            modules(appModule)
        }
    }
}
