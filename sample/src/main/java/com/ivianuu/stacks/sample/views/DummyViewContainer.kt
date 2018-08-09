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

import android.content.Context
import android.os.Parcelable
import android.util.AttributeSet
import android.util.SparseArray
import android.widget.FrameLayout
import com.ivianuu.stacks.Backstack
import com.ivianuu.stacks.sample.R
import com.ivianuu.stacks.sample.util.d
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.view_dummy.view.*

@Parcelize
data class DummyViewContainerKey(val count: Int) : ViewKey, Parcelable {
    override val layoutRes = R.layout.view_dummy_container
}

/**
 * @author Manuel Wrage (IVIanuu)
 */
class DummyViewContainer @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr), BackstackView {

    override lateinit var key: Any
    override lateinit var backstack: Backstack

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        val key = key as DummyViewContainerKey

        title.text = "Count: ${key.count}"
        next.setOnClickListener { backstack.goTo(DummyViewContainerKey(key.count + 1)) }
        prev.setOnClickListener { backstack.goBack() }
        go_to_root.setOnClickListener { backstack.jumpToRoot() }
        go_up_5.setOnClickListener {
            (1..5).forEach { backstack.goTo(DummyViewContainerKey(key.count + it)) }
        }

        go_to_third.setOnClickListener {
            backstack.setBackstack(backstack.getBackstack().take(3))
        }
    }

    override fun saveHierarchyState(container: SparseArray<Parcelable>?) {
        d { "save hierarchy state" }
        super.saveHierarchyState(container)
    }

    override fun restoreHierarchyState(container: SparseArray<Parcelable>?) {
        d { "restore hierarchy state" }
        super.restoreHierarchyState(container)
    }
}