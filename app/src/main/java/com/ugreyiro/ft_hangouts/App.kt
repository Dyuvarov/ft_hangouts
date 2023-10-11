package com.ugreyiro.ft_hangouts

import android.app.Application
import android.content.IntentFilter
import android.provider.Telephony
import com.ugreyiro.ft_hangouts.observer.MainActivityBackgroundObserver
import com.ugreyiro.ft_hangouts.observer.SmsPublisher

class App : Application() {

    override fun onCreate() {
        registerActivityLifecycleCallbacks(MainActivityBackgroundObserver())
        registerReceiver(SmsPublisher, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
        super.onCreate()
    }

    override fun onTerminate() {
        unregisterReceiver(SmsPublisher)
        super.onTerminate()
    }
}