package com.ivianuu.stacks

typealias Reducer = (currentBackstack: MutableList<Any>) -> Result

data class Result(
    val newBackstack: List<Any>,
    val direction: Direction
)