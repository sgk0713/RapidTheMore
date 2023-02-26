package kr.evangers.rapidthemore.ui.base

class Event<T> constructor(private val value: T) {
    private var hasBeenHandled = false

    fun getValueIfNotHandled(): T? = if (hasBeenHandled) {
        null
    } else {
        hasBeenHandled = true
        value
    }

    fun getValue(): T? {
        return value
    }
}