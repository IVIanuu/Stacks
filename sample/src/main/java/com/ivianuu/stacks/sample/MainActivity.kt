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

package com.ivianuu.stacks.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.ivianuu.stacks.Backstack
import com.ivianuu.stacks.StateChange
import com.ivianuu.stacks.StateChangeListener
import com.ivianuu.stacks.sample.fragments.FragmentStateChanger
import com.ivianuu.stacks.sample.fragments.FragmentsActivity
import com.ivianuu.stacks.sample.util.d
import com.ivianuu.stacks.sample.views.ViewsActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fragments.setOnClickListener { startActivity(Intent(this, FragmentsActivity::class.java)) }
        views.setOnClickListener { startActivity(Intent(this, ViewsActivity::class.java)) }
    }
}
