/*
 * Copyright 2018 Manuel Wrage
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivianuu.stacks

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity

/**
 * Informs listeners about lifecycle events
 * We cannot use activity lifecycle callbacks only because it doesn't work with fragments
 */
class LifecycleListener : Fragment(), Application.ActivityLifecycleCallbacks {

    private var act: FragmentActivity? = null
    private var hasRegisteredCallbacks = false

    private val listeners = mutableListOf<Listener>()

    override fun onResume() {
        super.onResume()
        listeners.forEach { it.onResume() }
    }

    override fun onPause() {
        super.onPause()
        listeners.forEach { it.onPause() }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
    }

    override fun onActivityStarted(activity: Activity) {
    }

    override fun onActivityResumed(activity: Activity) {
    }

    override fun onActivityPaused(activity: Activity) {
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        if (act == activity) {
            listeners.forEach { it.onSaveInstanceState(outState) }
        }
    }

    override fun onActivityStopped(activity: Activity) {
    }

    override fun onActivityDestroyed(activity: Activity) {
        if (act == activity) {
            activity.application.unregisterActivityLifecycleCallbacks(this)
        }
    }

    internal fun addListener(listener: Listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener)
        }
    }

    private fun registerActivityListener(activity: FragmentActivity) {
        this.act = activity

        if (!hasRegisteredCallbacks) {
            hasRegisteredCallbacks = true
            activity.application.registerActivityLifecycleCallbacks(this)
            activeLifecycleListeners[activity] = this
        }
    }

    internal interface Listener {
        fun onResume()

        fun onPause()

        fun onSaveInstanceState(outState: Bundle)
    }

    companion object {
        private const val FRAGMENT_TAG = "com.ivianuu.stacks.LifecycleListener"

        private val activeLifecycleListeners = mutableMapOf<Activity, LifecycleListener>()

        fun install(activity: FragmentActivity): LifecycleListener {
            var lifecycleListener = findInActivity(activity)
            if (lifecycleListener == null) {
                lifecycleListener = LifecycleListener()
                activity.supportFragmentManager.beginTransaction()
                    .add(lifecycleListener, FRAGMENT_TAG)
                    .commit()
            }
            lifecycleListener.registerActivityListener(activity)
            return lifecycleListener
        }

        private fun findInActivity(activity: FragmentActivity): LifecycleListener? {
            var lifecycleListener: LifecycleListener? = activeLifecycleListeners[activity]
            if (lifecycleListener == null) {
                lifecycleListener =
                        activity.supportFragmentManager
                            .findFragmentByTag(FRAGMENT_TAG) as LifecycleListener?
            }

            return lifecycleListener
        }

    }

}