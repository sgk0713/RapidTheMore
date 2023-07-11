package kr.evangers.rapidthemore.ui.base

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.lifecycle.LifecycleObserver
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.HiltAndroidApp
import kr.evangers.rapidthemore.ui.util.AppOpenAdManager
import javax.inject.Inject

@HiltAndroidApp
class BaseApp : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {

    private var currentActivity: Activity? = null

    @Inject
    lateinit var appOpenAdManager: AppOpenAdManager

    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        appOpenAdManager.loadAd(this)
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

    override fun onActivityStarted(activity: Activity) {
        // Updating the currentActivity only when an ad is not showing.
        if (appOpenAdManager.isShowingAd.not()) {
            currentActivity = activity
            currentActivity?.let {
                appOpenAdManager.showAdIfAvailable(it, object : OnShowAdCompleteListener {
                    override fun onShowAdComplete() {

                    }
                })
            }
        }
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}


    interface OnShowAdCompleteListener {
        fun onShowAdComplete()
    }


}