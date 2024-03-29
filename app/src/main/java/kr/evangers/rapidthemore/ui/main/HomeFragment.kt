package kr.evangers.rapidthemore.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import kr.evangers.rapidthemore.R
import kr.evangers.rapidthemore.databinding.FragmentHomeBinding
import kr.evangers.rapidthemore.ui.base.ParentFragment
import kr.evangers.rapidthemore.ui.util.AppOpenAdManager
import kr.evangers.rapidthemore.ui.util.longToast
import kr.evangers.rapidthemore.ui.util.shortToast
import java.text.DecimalFormat
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : ParentFragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var appOpenAdManager: AppOpenAdManager

    private lateinit var adView: AdView
    private var initialLayoutComplete = false
    private val adSize: AdSize
        get() {
            val display = requireActivity().windowManager.defaultDisplay
            val outMetrics = DisplayMetrics()
            display.getMetrics(outMetrics)

            val density = outMetrics.density

            var adWidthPixels = binding.adBottomBannerViewContainer.width.toFloat()
            if (adWidthPixels == 0f) {
                adWidthPixels = outMetrics.widthPixels.toFloat()
            }

            val adWidth = (adWidthPixels / density).toInt()
            return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(
                requireContext(),
                adWidth
            )
        }
    private var mRewardedAd: RewardedInterstitialAd? = null
    private var isRewardAdRequesting = false


    override fun bindView(view: View) {
        binding = FragmentHomeBinding.bind(view)
    }

    override fun initUi() {
        initialLayoutComplete = false
        with(binding) {
            naverPayButton.setOnClickListener { viewModel.launchNaverPay() }
            launchSPay.setOnClickListener { viewModel.launchSpay() }
            launchKbpay.setOnClickListener { viewModel.launchKbpay() }
            with(numberButtons) {
                button0.setOnClickListener { viewModel.addDigitLast(0) }
                button1.setOnClickListener { viewModel.addDigitLast(1) }
                button2.setOnClickListener { viewModel.addDigitLast(2) }
                button3.setOnClickListener { viewModel.addDigitLast(3) }
                button4.setOnClickListener { viewModel.addDigitLast(4) }
                button5.setOnClickListener { viewModel.addDigitLast(5) }
                button6.setOnClickListener { viewModel.addDigitLast(6) }
                button7.setOnClickListener { viewModel.addDigitLast(7) }
                button8.setOnClickListener { viewModel.addDigitLast(8) }
                button9.setOnClickListener { viewModel.addDigitLast(9) }
                buttonClear.setOnClickListener { viewModel.clearNumber() }
                buttonRemove.setOnClickListener { viewModel.removeDigitLast() }
            }
            adRewardViewContainer.setOnClickListener {
                if (mRewardedAd != null) {
                    appOpenAdManager.setAdDisplayable(false)
                    mRewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {

                        override fun onAdDismissedFullScreenContent() {
                            mRewardedAd = null
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            mRewardedAd = null
                            appOpenAdManager.setAdDisplayable(true)
                            loadReward()
                        }

                        override fun onAdShowedFullScreenContent() {
                            adRewardViewContainer.isVisible = false
                        }
                    }
                    mRewardedAd?.show(requireActivity()) {
                        viewModel.onUserRewarded(it.type, it.amount)
                    }
                }


            }
            adView = AdView(requireContext())
            adBottomBannerViewContainer.addView(adView)
            adBottomBannerViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
                if (!initialLayoutComplete) {
                    initialLayoutComplete = true
                    loadBanner()
                }
            }
            bannerWebView.settings.javaScriptEnabled = true
            val script = """
                <script src ="https://ads-partners.coupang.com/g.js"></script>
                <script>
                new PartnersCoupang.G({
                    ${getString(R.string.coupang_json)}
                });</script>
            """.trimIndent()
            val html = "<div>$script</div>"
            bannerWebView.loadData(html, "text/html", "utf-8")
            bannerWebView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    return if (url?.startsWith("http") == true) {
                        val intent = Intent(Intent.ACTION_VIEW, url?.toUri()).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        requireActivity().startActivity(intent)
                        true
                    } else {
                        super.shouldOverrideUrlLoading(view, url)
                    }
                }

                override fun onLoadResource(view: WebView?, url: String?) {
                    super.onLoadResource(view, url)
                }
            }
            loadReward()
        }
    }

    fun isAppInstalled(packageName: String): Boolean {
        return requireContext().packageManager.getLaunchIntentForPackage(packageName) != null
    }

    override fun initBinding() {
        viewModel.liveData.observe(viewLifecycleOwner) { state ->
            state.amount.getValueIfNotHandled()?.let {
                val formatter = DecimalFormat("#,###.##")
                val result = formatter.format(it)
                binding.numberTextView.text = result
            }
            state.ratio.getValueIfNotHandled()?.let {
                binding.resultTextView.text = getString(R.string.percent_with_number, it.toString())
                binding.twiceResultTextView.text =
                    getString(R.string.percent_with_number, it.times(2f).toString())
            }
            state.toastMessage?.getValueIfNotHandled()?.let {
                requireContext().shortToast(it)
            }
            state.longToastMessage?.getValueIfNotHandled()?.let {
                requireContext().longToast(it)
            }
            state.launchNaverPay?.getValueIfNotHandled()?.let {
                launchNaverPay()
            }
            state.launchSpay?.getValueIfNotHandled()?.let {
                launchSpay()
            }
            state.launchKbpay?.getValueIfNotHandled()?.let {
                launchKbpay()
            }
            state.intent?.getValueIfNotHandled()?.let {
                navToIntent(it)
            }
            state.adid?.getValueIfNotHandled()?.let { adidValue ->
                val adidKeyValue = "\"deviceId\": \"${adidValue}\""
                val json = getString(R.string.coupang_json) + ",${adidKeyValue}"
                val script = """
                <script src ="https://ads-partners.coupang.com/g.js"></script>
                <script>
                new PartnersCoupang.G({
                    $json
                });</script>
            """.trimIndent()
                val html = "<div>$script</div>"
                binding.bannerWebView.loadData(html, "text/html", "utf-8")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appOpenAdManager.setAdDisplayable(true)
    }

    override fun onViewCreatedSg(view: View, savedInstanceState: Bundle?) {
        super.onViewCreatedSg(view, savedInstanceState)
        viewModel.start()
        initUi()
        initBinding()
    }


    private fun launchNaverPay() {
        val packageName = getString(R.string.naverpay_package)
        if (isAppInstalled(packageName)) {
            viewModel.showPercentToast()
            val uri = Uri.Builder()
                .scheme(getString(R.string.naverpay_schme))
                .authority(getString(R.string.naverpay_host))
                .appendQueryParameter(
                    getString(R.string.naverpay_key_1),
                    getString(R.string.naverpay_value_1)
                )
                .appendQueryParameter(
                    getString(R.string.naverpay_key_2),
                    getString(R.string.naverpay_value_2)
                )
                .build()
            val intent = Intent(Intent.ACTION_VIEW, uri)
            viewModel.navToIntent(intent)
        } else {
            viewModel.showToast(getString(R.string.no_naver_app))
        }
    }

    private fun launchSpay() {
        val packageName = getString(R.string.spay_package)
        if (isAppInstalled(packageName)) {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
            intent?.let {
                viewModel.showPercentToast()
                viewModel.navToIntent(it)
            }
        } else {
            viewModel.showToast(getString(R.string.no_spay_app))
        }
    }

    private fun launchKbpay() {
        val packageName = getString(R.string.kbpay_package)
        if (isAppInstalled(packageName)) {
            val intent = requireContext().packageManager.getLaunchIntentForPackage(packageName)
            intent?.let {
                viewModel.showPercentToast()
                viewModel.navToIntent(it)
            }
        } else {
            viewModel.showToast(getString(R.string.no_kbpay_app))
        }
    }

    private fun navToIntent(intent: Intent) {
        requireContext().startActivity(intent)
    }

    private fun loadBanner() {
        adView.adUnitId = getString(R.string.admob_home_banner_unit_id)
        adView.setAdSize(adSize)
        val adRequest = AdRequest
            .Builder()
            .build()
        adView.loadAd(adRequest)
    }

    private fun loadReward() {
        if (isRewardAdRequesting || mRewardedAd != null) {
            binding.adRewardViewContainer.isVisible = false
            return
        }
        binding.adRewardViewContainer.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                R.color.button_border
            )
        )
        binding.rewardAdText.text = getString(R.string.ad_loading)
        isRewardAdRequesting = true
        val adRequest = AdRequest.Builder().build()
        RewardedInterstitialAd.load(requireContext(),
            getString(R.string.admob_reward_unit_id),
            adRequest,
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    super.onAdFailedToLoad(p0)
                    isRewardAdRequesting = false
                    mRewardedAd = null
                }

                override fun onAdLoaded(p0: RewardedInterstitialAd) {
                    super.onAdLoaded(p0)
                    binding.adRewardViewContainer.isVisible = true
                    binding.adRewardViewContainer.setBackgroundColor(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.ad_enabled
                        )
                    )
                    binding.rewardAdText.text = getString(R.string.ad_description)
                    isRewardAdRequesting = false
                    mRewardedAd = p0
                }
            })
    }

}