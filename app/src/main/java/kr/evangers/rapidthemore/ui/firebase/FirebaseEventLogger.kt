package kr.evangers.rapidthemore.ui.firebase

import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase
import javax.inject.Inject


class FirebaseEventLogger @Inject constructor(
) : EventLogger {

    private val firebaseAnalytics: FirebaseAnalytics by lazy {
        Firebase.analytics
    }

    override fun logEvent(eventName: String, eventArgs: Map<String, Any>?) {
        Log.d("RapidTheMore", "[Firebase] : ${eventName} : ${eventArgs}")
        firebaseAnalytics.logEvent(eventName, bundleFromMap(eventArgs))
    }
}

private fun bundleFromMap(eventArgs: Map<String, Any>?): Bundle? {
    return eventArgs?.let { bundleOf(*it.toList().toTypedArray()) }
}
