package kr.evangers.rapidthemore.ui.main

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.webkit.WebSettings
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kr.evangers.rapidthemore.R
import kr.evangers.rapidthemore.databinding.FragmentHomeBinding
import kr.evangers.rapidthemore.ui.base.ParentFragment
import kr.evangers.rapidthemore.ui.util.longToast
import kr.evangers.rapidthemore.ui.util.shortToast
import java.text.DecimalFormat

@AndroidEntryPoint
class HomeFragment : ParentFragment(R.layout.fragment_home) {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var binding: FragmentHomeBinding

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
            adView = AdView(requireContext())
            adBottomBannerViewContainer.addView(adView)
            adBottomBannerViewContainer.viewTreeObserver.addOnGlobalLayoutListener {
                if (!initialLayoutComplete) {
                    initialLayoutComplete = true
                    loadBanner()
                }
            }

            // Coupang 파트너스 배너 설정
            setupCoupangPartnersBanner()
        }
    }

    private fun setupCoupangPartnersBanner() {

        binding.coupangPartnersWebView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_NO_CACHE
        }

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body, html { margin: 0; padding: 0; width: 100%; height: 100%; }
                    .banner-container { width: 100%; display: flex; justify-content: center; align-items: center; }
                </style>
            </head>
            <body>
                <div class="banner-container">
                <script src="https://ads-partners.coupang.com/g.js"></script>
<script>
	${getString(R.string.coupang_ad_script)}
</script>
                </div>
            </body>
            </html>
        """.trimIndent()

        binding.coupangPartnersWebView.loadData(htmlContent, "text/html", "UTF-8")
    }

    fun isAppInstalled(packageName: String): Boolean {
        return requireContext().packageManager.getLaunchIntentForPackage(packageName) != null
    }

    override fun initBinding() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Coupang 파트너스 WebView를 사용하므로 기존 로직은 제거
                // 필요한 경우 여기에 WebView 관련 추가 로직 구현
            }
        }
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
        }
    }

    override fun onResume() {
        super.onResume()
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

}