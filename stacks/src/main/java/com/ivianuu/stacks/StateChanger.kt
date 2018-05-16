package com.ivianuu.stacks

/**
 * Reflects the state of the backstack
 */
interface StateChanger {

    fun handleStateChange(stateChange: StateChange, listener: Callback)

    interface Callback {
        fun onCompleted()
    }

}