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

package com.ivianuu.stacks.sample.fragments

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ivianuu.stacks.sample.R
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.view_dummy.go_to_root
import kotlinx.android.synthetic.main.view_dummy.go_to_third
import kotlinx.android.synthetic.main.view_dummy.go_up_5
import kotlinx.android.synthetic.main.view_dummy.next
import kotlinx.android.synthetic.main.view_dummy.prev
import kotlinx.android.synthetic.main.view_dummy.title

@Parcelize
data class DummyFragmentKey(val count: Int) : FragmentKey, Parcelable {
    override fun createFragment() = DummyFragment().apply {
        arguments = Bundle().apply {
            putParcelable("key", this@DummyFragmentKey)
        }
    }
}

/**
 * @author Manuel Wrage (IVIanuu)
 */
class DummyFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.view_dummy, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val stack = (activity as FragmentsActivity).router
        val key = arguments!!.getParcelable<DummyFragmentKey>("key")

        title.text = "Count: ${key.count}"
        next.setOnClickListener { stack.goTo(DummyFragmentKey(key.count + 1)) }
        prev.setOnClickListener { stack.goBack() }
        go_to_root.setOnClickListener { stack.jumpToRoot() }
        go_up_5.setOnClickListener {
            (1..5).forEach { stack.goTo(DummyFragmentKey(key.count + it)) }
        }

        go_to_third.setOnClickListener {
            stack.setBackstack(stack.getBackstack().take(3))
        }
    }

}