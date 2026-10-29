package ru.mipt.coffeemenu

import android.app.Application
import timber.log.Timber

class CoffeeApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
