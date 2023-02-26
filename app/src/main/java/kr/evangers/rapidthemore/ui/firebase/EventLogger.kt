package kr.evangers.rapidthemore.ui.firebase

interface EventLogger {
    fun logEvent(eventName: String, eventArgs: Map<String, Any>? = null)
}