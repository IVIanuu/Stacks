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

package com.ivianuu.stacks.sample.views

import android.os.Parcelable
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ivianuu.stacks.StateChange
import com.ivianuu.stacks.StateChanger

/**
 * @author Manuel Wrage (IVIanuu)
 */
class ViewStateChanger(
    private val container: ViewGroup,
    private val inflater: LayoutInflater
) : StateChanger {

    private val savedStates = mutableMapOf<Any, SparseArray<Parcelable>>()

    override fun handleStateChange(stateChange: StateChange, listener: StateChanger.Callback) {
        val topOldKey = stateChange.previousState.lastOrNull()
        if (topOldKey != null) {
            val oldView = container.getChildAt(0)

            if (stateChange.newState.contains(topOldKey)) {
                val savedState = SparseArray<Parcelable>()
                oldView.saveHierarchyState(savedState)
                savedStates[topOldKey] = savedState
            }

            container.removeView(oldView)
        }

        val topNewKey = stateChange.newState.lastOrNull()
        if (topNewKey != null) {
            val view = inflater
                .inflate((topNewKey as ViewKey).layoutRes, container, false)

            if (view is BackstackView) {
                view.backstack = stateChange.backstack
                view.key = topNewKey
            }

            val savedState = savedStates[topNewKey]
            if (savedState != null) {
                view.restoreHierarchyState(savedState)
            }

            container.addView(view)
        }

        savedStates.keys.toList().forEach { key ->
            if (!stateChange.newState.contains(key)) {
                savedStates.remove(key)
            }
        }

        listener.onCompleted()
    }

}