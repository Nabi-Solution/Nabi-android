package com.gdghufs.nabi

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NabiApplication : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}