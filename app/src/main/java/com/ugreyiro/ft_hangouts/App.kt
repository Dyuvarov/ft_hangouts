package com.ugreyiro.ft_hangouts

import android.app.Application
import com.ugreyiro.ft_hangouts.observer.MainActivityBackgroundObserver

class App : Application() {

    override fun onCreate() {
        registerActivityLifecycleCallbacks(MainActivityBackgroundObserver())
        super.onCreate()
    }

}