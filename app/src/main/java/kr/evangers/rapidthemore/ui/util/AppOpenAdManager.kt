package kr.evangers.rapidthemore.ui.util

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import kr.evangers.rapidthemore.R
import kr.evangers.rapidthemore.ui.base.BaseApp
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

class AppOpenAdManager @Inject constructor(
) {
    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var isAdDisplayable = AtomicBoolean(true)

    fun setAdDisplayable(display: Boolean) {
        isAdDisplayable.set(display)
    }

    fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable()) {
            return
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context, context.getString(R.string.admob_open_unit_id), request,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object : AppOpenAd.AppOpenAdLoadCallback() {

                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                }
            })
    }

    private fun isAdAvailable(): Boolean {
        return appOpenAd != null
    }

    fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: BaseApp.OnShowAdCompleteListener,
    ) {
        if (isAdDisplayable.get().not()) {
            return
        }

        if (!isAdAvailable()) {
            onShowAdCompleteListener.onShowAdComplete()
            loadAd(activity)
            return
        }

        appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

            override fun onAdDismissedFullScreenContent() {
                appOpenAd = null
                setAdDisplayable(false)

                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                appOpenAd = null
                setAdDisplayable(false)

                onShowAdCompleteListener.onShowAdComplete()
                loadAd(activity)
            }

            override fun onAdShowedFullScreenContent() {
            }
        }
        setAdDisplayable(false)
        appOpenAd?.show(activity)
    }
}