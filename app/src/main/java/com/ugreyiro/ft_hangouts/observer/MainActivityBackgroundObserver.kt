package com.ugreyiro.ft_hangouts.observer

import android.app.Activity
import android.app.Application.ActivityLifecycleCallbacks
import android.os.Bundle

class MainActivityBackgroundObserver : ActivityLifecycleCallbacks {
    companion object {
        var activitiesCounter = 0

        /**
         * True if app set in background.
         * There will be no activities when app set in background from main activity
         */
        fun setInBackground() = activitiesCounter == 0

        /**
         * True if app returned from background.
         * Because it's main activity, activities count will be 1 after returning
         */

        fun returnedToFront() = activitiesCounter == 1
    }

    override fun onActivityStarted(p0: Activity) {
        activitiesCounter++
    }

    override fun onActivityStopped(p0: Activity) {
        activitiesCounter--
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {}

    override fun onActivityResumed(p0: Activity) {}

    override fun onActivityPaused(p0: Activity) {}

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {}

    override fun onActivityDestroyed(p0: Activity) {}
}