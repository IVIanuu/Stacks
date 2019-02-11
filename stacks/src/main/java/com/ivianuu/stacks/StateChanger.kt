package com.ivianuu.stacks

/**
 * Reflects the state of the router
 */
interface StateChanger {

    fun handleStateChange(stateChange: StateChange, listener: () -> Unit)

}