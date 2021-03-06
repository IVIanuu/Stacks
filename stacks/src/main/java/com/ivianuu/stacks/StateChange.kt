package com.ivianuu.stacks

/**
 * Represents a state change
 */
class StateChange(
    val previousState: List<Any>,
    val newState: List<Any>,
    val direction: Direction,
    val router: Router
)